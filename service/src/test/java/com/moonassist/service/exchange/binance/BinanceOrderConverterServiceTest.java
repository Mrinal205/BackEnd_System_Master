package com.moonassist.service.exchange.binance;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.Order;
import com.moonassist.persistence.order.OrderRepository;
import com.moonassist.service.UserService;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.binance.BinanceOrderService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.binance.dto.trade.OrderStatus;
import org.knowm.xchange.binance.dto.trade.OrderType;
import org.knowm.xchange.binance.dto.trade.TimeInForce;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BinanceOrderConverter.class})
public class BinanceOrderConverterServiceTest {

  @Autowired
  private BinanceOrderConverter binanceOrderConverter;

  @MockBean
  private RequestDetail mockRequestDetail;

  @MockBean
  private BinanceExchange mockBinanceExchange;

  @MockBean
  private BinanceTradeService mockTradeService;

  @MockBean
  private OrderRepository mockOrderRepository;

  @MockBean
  private UserService mockUserService;

  @Test
  public void testConvertOrder() {

    String symbolPair = "XRPETH";
    BigDecimal price = new BigDecimal("3.4566");
    BigDecimal amount = new BigDecimal("231");

    BinanceOrder binanceOrder = new BinanceOrder(symbolPair, 1234l, "123456", price,
        amount, BigDecimal.ZERO, OrderStatus.NEW, TimeInForce.GTC, OrderType.LIMIT,
        OrderSide.BUY, BigDecimal.ZERO, BigDecimal.ZERO, 0L);

    Order order = binanceOrderConverter.convert(binanceOrder, Optional.empty());

    Assert.assertEquals(ExchangeEnum.BINANCE, order.getExchangeName());
    Assert.assertEquals(price, order.getPrice());
    Assert.assertEquals(amount, order.getAmount());
    Assert.assertEquals(price.multiply(amount), order.getTotal());
  }

}
