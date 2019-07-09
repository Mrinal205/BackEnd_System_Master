package com.moonassist.service.exchange.bittrex;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.bittrex.BittrexOrderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.bittrex.dto.trade.BittrexOpenOrder;
import org.knowm.xchange.bittrex.service.BittrexTradeService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BittrexOrderService.class})

public class BittrexOrderServiceTest {

  @Autowired
  private BittrexOrderService bittrexOrderService;

  @MockBean
  private RequestDetail requestDetail;

  @Mock
  private BittrexExchange mockBittrexExchange;

  @Mock
  private BittrexTradeService mockTradeService;

  @Test
  public void testConvertOrder_limitBuy() {

    String symbolPair = "XRP-ETH";
    BigDecimal price = new BigDecimal("3.4566");
    BigDecimal amount = new BigDecimal("231");

    Mockito.when(requestDetail.forRequest()).thenReturn(mockBittrexExchange);
    Mockito.when(mockBittrexExchange.getTradeService()).thenReturn(mockTradeService);

    BittrexOpenOrder bittrexOrder = new BittrexOpenOrder(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        symbolPair,
        "LIMIT_BUY",
        amount,
        BigDecimal.ZERO,
        price,
        BigDecimal.ZERO,
        price,
        BigDecimal.ZERO,
        "2018-09-20T12:23:50.573",
        null,
        null,
        null,
        null,
        null,
        null
    );

    Order order = bittrexOrderService.convert(bittrexOrder);

    Assert.assertEquals(ExchangeEnum.BITTREX, order.getExchangeName());
    Assert.assertEquals(price, order.getPrice());
    Assert.assertEquals(amount, order.getAmount());
    Assert.assertEquals(price.multiply(amount), order.getTotal());
    Assert.assertEquals(OfferType.BUY, order.getOfferType());
    Assert.assertEquals(OrderType.LIMIT, order.getOrderType());
  }

  @Test
  public void testConvertOrder_marketSell() {

    String symbolPair = "XRP-ETH";
    BigDecimal price = new BigDecimal("3.4566");
    BigDecimal amount = new BigDecimal("231");

    Mockito.when(requestDetail.forRequest()).thenReturn(mockBittrexExchange);
    Mockito.when(mockBittrexExchange.getTradeService()).thenReturn(mockTradeService);

    BittrexOpenOrder bittrexOrder = new BittrexOpenOrder(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        symbolPair,
        "MARKET_SELL",
        amount,
        BigDecimal.ZERO,
        amount,
        BigDecimal.ZERO,
        price,
        BigDecimal.ZERO,
        "2018-09-20T12:23:50.573",
        null,
        null,
        null,
        null,
        null,
        null
    );

    Order order = bittrexOrderService.convert(bittrexOrder);

    Assert.assertEquals(ExchangeEnum.BITTREX, order.getExchangeName());
    Assert.assertEquals(price, order.getPrice());
    Assert.assertEquals(amount, order.getAmount());
    Assert.assertEquals(price.multiply(amount), order.getTotal());
    Assert.assertEquals(OfferType.SELL, order.getOfferType());
    Assert.assertEquals(OrderType.MARKET, order.getOrderType());
  }

}
