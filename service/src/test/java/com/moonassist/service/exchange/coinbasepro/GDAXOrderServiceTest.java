package com.moonassist.service.exchange.coinbasepro;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.Order;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.coinbasepro.GDAXOrderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderStatus;
import org.knowm.xchange.binance.dto.trade.OrderType;
import org.knowm.xchange.binance.dto.trade.TimeInForce;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinbasepro.dto.trade.CoinbaseProOrder;
import org.knowm.xchange.coinbasepro.service.CoinbaseProTradeService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GDAXOrderService.class})

public class GDAXOrderServiceTest {

  @Autowired
  private GDAXOrderService gdaxOrderService;

  @MockBean
  private RequestDetail requestDetail;

  @Mock
  private CoinbaseProExchange mockCoinbaseProExchange;

  @Mock
  private CoinbaseProTradeService mockTradeService;

  @Test
  public void testConvertOrder() {

    String symbolPair = "ETH-EUR";
    BigDecimal price = new BigDecimal("3.4566");
    BigDecimal amount = new BigDecimal("231");

    Mockito.when(requestDetail.forRequest()).thenReturn(mockCoinbaseProExchange);
    Mockito.when(mockCoinbaseProExchange.getTradeService()).thenReturn(mockTradeService);

    CoinbaseProOrder gdaxOrder = new CoinbaseProOrder("1234", price, amount, symbolPair, "BUY",
      "2018-09-20T12:23:50.573", "2018-09-20T12:23:50.573", BigDecimal.TEN,
      BigDecimal.ZERO,  "OPEN", false, "limit", "", BigDecimal.ZERO, "", BigDecimal.ZERO);

    Order order = gdaxOrderService.convert(gdaxOrder, Optional.empty());

    Assert.assertEquals(ExchangeEnum.GDAX, order.getExchangeName());
    Assert.assertEquals(price, order.getPrice());
    Assert.assertEquals(amount, order.getAmount());
    Assert.assertEquals(price.multiply(amount), order.getTotal());
    Assert.assertNotNull(order.getTimestamp());
  }

}
