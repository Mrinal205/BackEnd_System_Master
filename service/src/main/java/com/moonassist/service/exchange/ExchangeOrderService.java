package com.moonassist.service.exchange;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.converters.OrderConverter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.binance.service.BinanceTradeHistoryParams;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.exceptions.NotYetImplementedForExchangeException;
import org.knowm.xchange.service.trade.params.CancelOrderByIdParams;
import org.knowm.xchange.service.trade.params.DefaultCancelOrderParamId;
import org.knowm.xchange.service.trade.params.orders.DefaultOpenOrdersParamCurrencyPair;
import org.knowm.xchange.service.trade.params.orders.OpenOrdersParamCurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service("ExchangeOrderService")
public class ExchangeOrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeOrderService.class);

  public static final List<String> KNOWN_EXCHANGE_ERRORS = ImmutableList.of("INSUFFICIENT_FUNDS");


  /*** Order Lookup ***/

  public List<Order> findOpenOrders(final RequestDetail requestDetail) {
    throw new NotYetImplementedForExchangeException("Find Open Orders is not implemented for " + requestDetail.getExchangeEnum().get());
  }

  public List<Order> findClosedOrders(final RequestDetail requestDetail) {
    throw new NotYetImplementedForExchangeException("Find Closed Orders is not implemented for " + requestDetail.getExchangeEnum().get());
  }

  public List<Order> findAllOrders(final RequestDetail requestDetail) {

    List<Order> openOrders = findOpenOrders(requestDetail);
    List<Order> closedOrders = findClosedOrders(requestDetail);

    openOrders.addAll(closedOrders);

    return ImmutableList.copyOf(openOrders);
  }


  public List<Order> findAllOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    List<Order> openOrders = findOpenOrders(requestDetail, symbolPair);
    List<Order> closedOrders = findClosedOrders(requestDetail, symbolPair);

    openOrders.addAll(closedOrders);

    return ImmutableList.copyOf(openOrders);
  }

  public List<Order> findOpenOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    OpenOrdersParamCurrencyPair params = new DefaultOpenOrdersParamCurrencyPair();
    params.setCurrencyPair(buildCurencyPair(symbolPair));

    OpenOrders rawOpenOrders = requestDetail.forRequest().getTradeService().getOpenOrders(params);
    LOGGER.debug("Found Open Orders: " + rawOpenOrders);

    return rawOpenOrders.getOpenOrders().stream()
        .map( order -> OrderConverter.convert(order) )
        .collect( Collectors.toList() );
  }

  public List<Order> findClosedOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    //TODO this seems to work but is it right?
    BinanceTradeHistoryParams params = new BinanceTradeHistoryParams(currencyPair);

    UserTrades userTrades = requestDetail.forRequest().getTradeService().getTradeHistory(params);
    LOGGER.debug("Found Closed Orders: " + userTrades);

    return userTrades.getUserTrades().stream()
        .map( userTrade -> OrderConverter.convert(userTrade) )
        .collect( Collectors.toList() );
  }

  /***   Order Placement ***/

  public boolean cancelOrder(final RequestDetail requestDetail, final String exchangeOrderId, final String symbolPair) throws IOException {

    CancelOrderByIdParams cancelOrderByIdParams = new DefaultCancelOrderParamId(exchangeOrderId);

    return requestDetail.forRequest().getTradeService().cancelOrder(cancelOrderByIdParams);
  }

  @SneakyThrows
  public Order placeOrder(final RequestDetail requestDetail, final Order order) {
    try {

      switch (order.getOrderType()) {

        case LIMIT:
          return placeLimit(requestDetail, order);

        case MARKET:
          return placeMarket(requestDetail, order);

        default:
          throw new IllegalStateException("Unknown order type [" + order.getOrderType() + "]");
      }

    } catch (org.knowm.xchange.exceptions.ExchangeException e){

      if (KNOWN_EXCHANGE_ERRORS.contains(e.getMessage())) {
        LOGGER.info("Received Exchange Error for Order Request", e.getMessage());
        throw new ExchangeException(e.getMessage());
      }
      else {
        LOGGER.error("Received Exchange Error for Order Request", e.getMessage());
        throw new ExchangeException(e);
      }

    }

  }

  @SneakyThrows
  protected Order placeLimit(final RequestDetail requestDetail, final Order order) {

    Preconditions.checkState(requestDetail.forRequest() != null, "Connection not initialized");

    LimitOrder limitOrder = buildLimit(requestDetail, order);
    final String exchangeOrderId = requestDetail.forRequest().getTradeService().placeLimitOrder(limitOrder);
    LOGGER.info("Placing Limit Order [" + limitOrder + "]");

    return order.toBuilder()
        .exchangeOrderId(exchangeOrderId)
        .amount(limitOrder.getOriginalAmount())
        .price(limitOrder.getLimitPrice())
        .build();
  }

  @SneakyThrows
  protected Order placeMarket(final RequestDetail requestDetail, final Order order) {

    MarketOrder marketOrder = buildMarket(requestDetail, order);

    Exchange exchange = requestDetail.forRequest();

    final String exchangeOrderId = exchange.getTradeService().placeMarketOrder(marketOrder);
    LOGGER.info("Placing MarketOrder [" + marketOrder + "]");

    return order.toBuilder()
        .exchangeOrderId(exchangeOrderId)
        .amount(marketOrder.getOriginalAmount())
        .build();
  }

  protected MarketOrder buildMarket(final RequestDetail requestDetail, final Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    return new MarketOrder.Builder(orderType, currencyPair)
        .originalAmount(order.getAmount())
        .build();
  }

  protected LimitOrder buildLimit(final RequestDetail requestDetail, final Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    //XChange's order type is our offer type --
    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    return new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(order.getPrice())
        .originalAmount(order.getAmount())
        .build();
  }

  protected CurrencyPair buildCurencyPair(final String input) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(input), "input can not be empty");
    Preconditions.checkArgument(input.contains("/"), "[" + input + "] input does not contain /");

    String[] symbols = input.split("/");
    return new CurrencyPair(Currency.getInstance(symbols[0]), Currency.getInstance(symbols[1]));
  }

}
