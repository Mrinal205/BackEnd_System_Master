package com.moonassist.service.exchange.bittrex;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OrderStatus;
import com.moonassist.bind.order.OrderType;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.ExchangeOrderService;

import org.knowm.xchange.binance.service.BinanceTradeHistoryParams;
import org.knowm.xchange.bittrex.BittrexAdapters;
import org.knowm.xchange.bittrex.BittrexUtils;
import org.knowm.xchange.bittrex.dto.trade.BittrexLimitOrder;
import org.knowm.xchange.bittrex.dto.trade.BittrexOpenOrder;
import org.knowm.xchange.bittrex.dto.trade.BittrexUserTrade;
import org.knowm.xchange.bittrex.service.BittrexTradeServiceRaw;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.service.exchange.converters.OrderConverter;

import lombok.SneakyThrows;

@Service
public class BittrexOrderService extends ExchangeOrderService {

  private final static Logger LOGGER = LoggerFactory.getLogger(BittrexOrderService.class);

  private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

  @Override
  @SneakyThrows
  public List<Order> findOpenOrders(final RequestDetail requestDetail) {

    BittrexTradeServiceRaw bittrexTradeServiceRaw = (BittrexTradeServiceRaw) requestDetail.forRequest().getTradeService();

    List<BittrexOpenOrder> bittrexOpenOrders = bittrexTradeServiceRaw.getBittrexOpenOrders(null);

    return bittrexOpenOrders.stream()
        .map(order -> convert(order))
        .collect(Collectors.toList());
  }

  @Override
  @SneakyThrows
  public List<Order> findClosedOrders(final RequestDetail requestDetail) {

    BittrexTradeServiceRaw bittrexTradeServiceRaw = (BittrexTradeServiceRaw) requestDetail.forRequest().getTradeService();

    List<BittrexUserTrade> bittrexTradeHistory = bittrexTradeServiceRaw.getBittrexTradeHistory(null);

    return bittrexTradeHistory.stream()
        .map( tradeHistory -> {

          String symbolPair = Joiner.on("/").join(
              Lists.reverse( Lists.newArrayList(Splitter.on('-').split(tradeHistory.getExchange())) )
          );

          return Order.builder()
              .exchangeOrderId(tradeHistory.getOrderUuid())
              .filled(ONE_HUNDRED)
              .price(tradeHistory.getPricePerUnit())
              .amount(tradeHistory.getQuantity())
              .symbolPair(symbolPair)
              .exchangeName(ExchangeEnum.BITTREX)
              .status(OrderStatus.TRADED)
              .offerType(tradeHistory.getOrderType().contains("SELL") ? OfferType.SELL : OfferType.BUY)
              .orderType(tradeHistory.getOrderType().contains("LIMIT") ? OrderType.LIMIT : OrderType.MARKET)
              .timestamp(BittrexUtils.toDate(tradeHistory.getTimeStamp()))
              .build();
        })
        .collect(Collectors.toList());
  }

  public List<Order> findClosedOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    return findClosedOrders(requestDetail).stream()
        .filter( order -> order.getStatus() == OrderStatus.TRADED || order.getStatus() == OrderStatus.CANCELED)
        .collect(Collectors.toList());
  }

  @VisibleForTesting
  protected Order convert(final BittrexOpenOrder bittrexOpenOrder) {

    BittrexLimitOrder bittrexLimitOrder = BittrexAdapters.adaptOpenOrder(bittrexOpenOrder);

    OrderType orderType = bittrexOpenOrder.getOrderType().contains("LIMIT") ?
        OrderType.LIMIT :
        OrderType.MARKET;

    OfferType offerType = bittrexOpenOrder.getOrderType().contains("SELL") ?
        OfferType.SELL :
        OfferType.BUY;

    Date timestamp = Date.from(LocalDateTime.parse(bittrexOpenOrder.getOpened()).atZone(ZoneId.systemDefault()).toInstant());
    BigDecimal price = (OrderType.LIMIT == orderType) ?
        bittrexOpenOrder.getLimit() :
        bittrexOpenOrder.getPrice();

    return Order.builder()
        .symbolPair(bittrexLimitOrder.getCurrencyPair().toString())
        .exchangeOrderId(bittrexOpenOrder.getOrderUuid())
        .amount(bittrexOpenOrder.getQuantity())
        .price(price)
        .orderType( orderType )
        .offerType( offerType )
        .exchangeName(ExchangeEnum.BITTREX)
        .amount(bittrexOpenOrder.getQuantity())
        .timestamp(timestamp)
        .filled(bittrexOpenOrder.getQuantity().subtract(bittrexOpenOrder.getQuantityRemaining()))
        .status(OrderStatus.OPEN)
        .build();
  }

  /**
   *
   * Places a Fake Market order for Bittrex. Fake in the sense that it is really a limit order that is below
   * the current Limit price for the quantity requested.
   *
   * It is expected that the Exchange will book the order at the highest ASK price, for each quantity it can
   * full fill.
   *
   * @param order
   * @return
   */
  @SneakyThrows
  @Override
  protected Order placeMarket(final RequestDetail requestDetail, final Order order) {

    final BigDecimal finalAdjustedPrice = findCorrectPriceForQuantity(requestDetail, order.getSymbolPair(), order.getAmount(), order.getOfferType());
    LOGGER.info("Adjusting BITTREX order [" + order.getId() + "] from price [" + order.getPrice() + "] to [" + finalAdjustedPrice + "]");
    order.setPrice(finalAdjustedPrice);

    LimitOrder limitOrder = buildLimit(requestDetail, order);

    final String exchangeOrderId = requestDetail.forRequest().getTradeService().placeLimitOrder(limitOrder);

    return order.toBuilder()
        .exchangeOrderId(exchangeOrderId)
        .build();
  }

  @VisibleForTesting
  @SneakyThrows
  protected BigDecimal findCorrectPriceForQuantity(final RequestDetail requestDetail, final String symbolPair, BigDecimal amount, final OfferType offerType) {

    final CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    OrderBook orderBook = requestDetail.forRequest().getMarketDataService().getOrderBook(currencyPair);

    BigDecimal trackingPrice = BigDecimal.ZERO;
    BigDecimal trackingAmount = BigDecimal.ZERO;

    List<LimitOrder> orderList = (OfferType.BUY == offerType) ?
        orderBook.getAsks() :
        orderBook.getBids();

    int index = 0;
    while (trackingAmount.compareTo(amount) < 0 && index < orderList.size()) {
      LimitOrder order = orderList.get(index);

      trackingAmount = trackingAmount.add(order.getOriginalAmount());
      trackingPrice = order.getLimitPrice();
      index++;
    }

    return trackingPrice;
  }

}
