package com.moonassist.service.exchange.kucoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.moonassist.bind.order.OfferType;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.bind.kucoin.CoinInfo;
import com.moonassist.service.bind.kucoin.ExchangeInfo;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.exchange.converters.OrderConverter;
import com.moonassist.service.util.RoundingUtil;
import com.moonassist.service.util.SymbolUtils;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.kucoin.service.KucoinCancelOrderParams;
import org.knowm.xchange.kucoin.service.KucoinTradeHistoryParams;
import org.knowm.xchange.service.trade.params.CancelOrderParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KucoinOrderService extends ExchangeOrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(KucoinOrderService.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final int MAX_ORDERS_PAGE_SIZE = 20;


  @Override
  public boolean cancelOrder(final RequestDetail requestDetail, final String exchangeOrderId, final String symbolPair) throws IOException {

     CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    CancelOrderParams kucoinCancelOrderParams = new KucoinCancelOrderParams(currencyPair, exchangeOrderId, Order.OrderType.BID);

    requestDetail.forRequest().getTradeService().cancelOrder(kucoinCancelOrderParams);

    //To avoid asking the client to send in BID or ASK, along with the exchangeOrderId, just call it twice. Why Kucoin asks for BID or ASK and the orderId who knows!!
    kucoinCancelOrderParams = new KucoinCancelOrderParams(currencyPair, exchangeOrderId, Order.OrderType.ASK);
    return requestDetail.forRequest().getTradeService().cancelOrder(kucoinCancelOrderParams);
  }

  @Override
  public List<com.moonassist.bind.order.Order> findClosedOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    //TODO need to handle paging better
    KucoinTradeHistoryParams params = new KucoinTradeHistoryParams();
    params.setCurrencyPair(currencyPair);
    params.setPageLength(MAX_ORDERS_PAGE_SIZE);
    params.setPageNumber(0);

    UserTrades userTrades = requestDetail.forRequest().getTradeService().getTradeHistory(params);
    LOGGER.debug("Found Closed Orders: " + userTrades);

    return userTrades.getUserTrades().stream()
        .map( userTrade -> OrderConverter.convert(userTrade) )
        .collect( Collectors.toList() );
  }

  @Override
  protected MarketOrder buildMarket(final RequestDetail requestDetail, final com.moonassist.bind.order.Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    ExchangeInfo exchangeInformation = exchangeInformation();
    final CoinInfo coinInfoAmount = exchangeInformation.symbolsMap().get(SymbolUtils.base(order.getSymbolPair()));
    final BigDecimal amount = massageAmount(coinInfoAmount, order.getAmount());

    return new MarketOrder.Builder(orderType, currencyPair)
        .originalAmount(amount)
        .build();
  }

  @Override
  protected LimitOrder buildLimit(final RequestDetail requestDetail, final com.moonassist.bind.order.Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    //XChange's order type is our offer type --
    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    ExchangeInfo exchangeInformation = exchangeInformation();

    final CoinInfo coinInfoAmount = exchangeInformation.symbolsMap().get(SymbolUtils.base(order.getSymbolPair()));
    final BigDecimal amount = massageAmount(coinInfoAmount, order.getAmount());

    final CoinInfo priceCoinInfo = exchangeInformation.symbolsMap().get(SymbolUtils.count(order.getSymbolPair()));
    final BigDecimal price = massagePrice(priceCoinInfo, order.getPrice());

    return new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(price)
        .originalAmount(amount)
        .build();
  }

  @VisibleForTesting
  protected BigDecimal massageAmount(final CoinInfo coinInfo, final BigDecimal amount) {

    return RoundingUtil.round(amount, coinInfo.getTradePrecision(), RoundingMode.DOWN);
  }

  private BigDecimal massagePrice(final CoinInfo coinInfo, final BigDecimal price) {
    //TODO should we always round down, or should it be based off of BID / ASK?
    return RoundingUtil.round(price, coinInfo.getTradePrecision(), RoundingMode.DOWN);
  }

  /**
   * Results from calling `https://api.kucoin.com/v1/market/open/coins`
   * @return
   * @throws IOException
   */
  @SneakyThrows
  protected ExchangeInfo exchangeInformation() {

    //TODO make this part of the singleton

    String file = new String(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("kucoin.exchange.json")));
    Preconditions.checkState(StringUtils.isNotEmpty(file), "Error looking up Binance Exchange Info File");

    return MAPPER.readValue(file, ExchangeInfo.class);
  }

}
