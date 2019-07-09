package com.moonassist.service.exchange.coinbasepro;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.knowm.xchange.coinbasepro.CoinbaseProAdapters;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrder;
import org.knowm.xchange.coinbasepro.service.CoinbaseProTradeServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyMetaData;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.UserTrades;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderStatus;
import com.moonassist.bind.order.OrderType;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.util.RoundingUtil;

import lombok.SneakyThrows;

@Service
public class GDAXOrderService extends ExchangeOrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GDAXOrderService.class);

  MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_UP);


  @Override
  @SneakyThrows
  public List<Order> findOpenOrders(final RequestDetail requestDetail) {

    CoinbaseProTradeServiceRaw CoinbaseProTradeServiceRaw = (CoinbaseProTradeServiceRaw) requestDetail.forRequest().getTradeService();

    return Arrays.stream(CoinbaseProTradeServiceRaw.getCoinbaseProOpenOrders())
        .map(order -> convert(order, Optional.empty()))
        .collect(Collectors.toList());
  }

  @SneakyThrows
  @Override
  public List<Order> findClosedOrders(final RequestDetail requestDetail) {

    CoinbaseProTradeServiceRaw coinbaseProTradeServiceRaw = (CoinbaseProTradeServiceRaw) requestDetail.forRequest().getTradeService();

    List<CoinbaseProOrder> orders = Arrays.asList(coinbaseProTradeServiceRaw.getOrders("done"));

    return orders.stream()
        .map(order -> {
          Optional<UserTrades> userTrades = Optional.empty();

          return convert(order, userTrades);
        })
        .collect(Collectors.toList());
  }

  public List<Order> findClosedOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    CoinbaseProTradeServiceRaw coinbaseProTradeServiceRaw = (CoinbaseProTradeServiceRaw) requestDetail.forRequest().getTradeService();

    List<CoinbaseProOrder> orders = Arrays.asList(coinbaseProTradeServiceRaw.getOrders("done"));

    return orders.stream()
        .filter( order -> symbolPair.equals(order.getProductId().replace("-", "/")) )
        .map(order -> {
          Optional<UserTrades> userTrades = Optional.empty();

          return convert(order, userTrades);
        })
        .collect(Collectors.toList());
  }

  @SneakyThrows
  private UserTrades fetchTrades(final RequestDetail requestDetail, final CurrencyPair currencyPair) {

    CoinbaseProTradeServiceRaw.GdaxTradeHistoryParams params = new CoinbaseProTradeServiceRaw.GdaxTradeHistoryParams();
    params.setCurrencyPair(currencyPair);
    return requestDetail.forRequest().getTradeService().getTradeHistory(params);
  }

  /**
   *
   * Lars [1:03 PM]
   * So for market orders you have the `size` + `executed_value`. With these we can calculate the price pretty easily I believe:
   * `executed_value`/`size`=`price`
   *
   * @Eric does this make sense?
   *
   * @param intput
   * @return
   */
  private CurrencyPair fromRawInput(final String intput) {
    return new CurrencyPair(intput.replace("-", "/"));
  }

  @VisibleForTesting
  protected Order convert(final CoinbaseProOrder coinbaseProOrder, final Optional<UserTrades> userTrades) {

    org.knowm.xchange.dto.Order order = CoinbaseProAdapters.adaptOrder(coinbaseProOrder);

    LOGGER.debug("Processing id[" + coinbaseProOrder.getId() + "]");

    BigDecimal price = ("market".equals(coinbaseProOrder.getType()) && coinbaseProOrder.getFilledSize().compareTo(BigDecimal.ZERO) > 0) ?
        coinbaseProOrder.getExecutedvalue().divide(coinbaseProOrder.getFilledSize(), MATH_CONTEXT) :
        coinbaseProOrder.getPrice();

    return Order.builder()
        .symbolPair(fromRawInput(coinbaseProOrder.getProductId()).toString())
        .price(price)
        .status("done".equals(coinbaseProOrder.getStatus()) ? OrderStatus.TRADED : OrderStatus.OPEN)
        .amount(coinbaseProOrder.getSize())
        .exchangeOrderId(coinbaseProOrder.getId())
        .exchangeName(ExchangeEnum.GDAX)
        .filled(coinbaseProOrder.getFilledSize())
        .offerType(coinbaseProOrder.getSide().equals("buy") ? OfferType.BUY : OfferType.SELL)
        .orderType(coinbaseProOrder.getType().equals("limit") ? OrderType.LIMIT : OrderType.MARKET)
        .filled(coinbaseProOrder.getFilledSize())
        .timestamp(order.getTimestamp())
        .build();
  }

  //TODO refactor to clean up duplicate code
  @Override
  protected MarketOrder buildMarket(final RequestDetail requestDetail, final Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    Integer roundingScale = getRoundingPlaces(requestDetail, order);
    final BigDecimal amount = RoundingUtil.round(order.getAmount(), roundingScale, RoundingMode.DOWN);
    if (amount.compareTo(order.getAmount()) != 0) {
      LOGGER.info("Adjusted orderId[" + order.getId() + "] amount from [" + order.getAmount() + "] to [" + amount + "]");
    }

    return new MarketOrder.Builder(orderType, currencyPair)
        .originalAmount(amount)
        .build();
  }

  @Override
  protected LimitOrder buildLimit(final RequestDetail requestDetail, final Order order) {
    Preconditions.checkArgument(order != null, "order argument can not be null");
    Preconditions.checkArgument(order.getAmount() != null, "Limit Orders must have an amount");

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    //XChange's order type is our offer type --
    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    Integer roundingScale = getRoundingPlaces(requestDetail, order);
    final BigDecimal amount = RoundingUtil.round(order.getAmount(), roundingScale, RoundingMode.DOWN);
    if (amount.compareTo(order.getAmount()) != 0) {
      LOGGER.info("Adjusted orderId[" + order.getId() + "] amount from [" + order.getAmount() + "] to [" + amount + "]");
    }

    return new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(order.getPrice())
        .originalAmount(amount)
        .build();
  }

  @VisibleForTesting
  protected Integer getRoundingPlaces(final RequestDetail requestDetail, final Order order) {


    CurrencyPair currencyPair = new CurrencyPair(order.getSymbolPair());

    Map<CurrencyPair, CurrencyPairMetaData> currencyPairCurrencyPairMetaDataMap = requestDetail.forRequest().getExchangeMetaData().getCurrencyPairs();

    Preconditions.checkState(currencyPairCurrencyPairMetaDataMap.containsKey(currencyPair), "No found currency for map value " + currencyPair);
    CurrencyPairMetaData currencyPairMetaData = currencyPairCurrencyPairMetaDataMap.get(currencyPair);

    return RoundingUtil.places(currencyPairMetaData.getMinimumAmount());
  }

}
