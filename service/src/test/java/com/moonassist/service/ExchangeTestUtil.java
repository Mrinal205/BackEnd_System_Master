package com.moonassist.service;

import com.google.common.collect.ImmutableMap;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.http.cookie.SM;

public class ExchangeTestUtil {

  private static final Order EOS_ETH_ORDER = Order.builder()
      .symbolPair("EOS/ETH")
      .orderType(OrderType.LIMIT)
      .offerType(OfferType.BUY)
      .price(new BigDecimal("0.005"))
      .amount(new BigDecimal(2))
      .build();

  private static final Order OMG_ETH_ORDER = Order.builder()
      .symbolPair("OMG/ETH")
      .orderType(OrderType.LIMIT)
      .offerType(OfferType.BUY)
      .price(new BigDecimal("0.005"))
      .amount(new BigDecimal(2))
      .build();

  private static final Order ETH_BTC_ORDER = Order.builder()
      .symbolPair("ETH/BTC")
      .orderType(OrderType.LIMIT)
      .offerType(OfferType.SELL)
      .price(new BigDecimal("0.065"))
      .amount(new BigDecimal(0.07))
      .build();

  private static final Order ETH_EUR_ORDER = Order.builder()
      .symbolPair("ETH/EUR")
      .orderType(OrderType.LIMIT)
      .offerType(OfferType.BUY)
      .price(new BigDecimal("150"))
      .amount(new BigDecimal("0.06768109"))
      .build();

  private static final Order SMALL_ETH_BTC_ORDER = Order.builder()
      .symbolPair("ETH/BTC")
      .orderType(OrderType.MARKET)
      .offerType(OfferType.BUY)
      .amount(new BigDecimal(0.01883168))
      .build();

  private static final Order SMALL_ETH_EUR_ORDER = Order.builder()
      .symbolPair("ETH/EUR")
      .orderType(OrderType.MARKET)
      .offerType(OfferType.BUY)
      .amount(new BigDecimal("0.040"))
      .build();

  private static final Order SMALL_XRP_ETH_ORDER = Order.builder()
      .symbolPair("XRP/ETH")
      .orderType(OrderType.MARKET)
      .offerType(OfferType.BUY)
      .amount(new BigDecimal(5))
      .build();

  public static final Order CRAZY_ORDER = Order.builder()
      .symbolPair("XRP/ETH")
      .orderType(OrderType.LIMIT)
      .offerType(OfferType.BUY)
      .price(new BigDecimal("0.0005"))
      .amount(new BigDecimal(20))
      .build();


  public static final Map<ExchangeEnum, Order> UNREASONABLE_LIMIT_ORDERS = ImmutableMap.<ExchangeEnum, Order>builder()
      .put(ExchangeEnum.BINANCE, OMG_ETH_ORDER)
      .put(ExchangeEnum.BITTREX, OMG_ETH_ORDER)
      .put(ExchangeEnum.GDAX, ETH_EUR_ORDER)
      .put(ExchangeEnum.KRAKEN, EOS_ETH_ORDER)
      .put(ExchangeEnum.KUCOIN, OMG_ETH_ORDER)
      .put(ExchangeEnum.POLONIEX, OMG_ETH_ORDER)
      .build();

  public static final Map<ExchangeEnum, Order> SMALL_REASONABLE_MARKET_ORDERS = ImmutableMap.<ExchangeEnum, Order>builder()
      .put(ExchangeEnum.GDAX, SMALL_ETH_EUR_ORDER)
      .put(ExchangeEnum.BITTREX, SMALL_XRP_ETH_ORDER)
      .build();

}
