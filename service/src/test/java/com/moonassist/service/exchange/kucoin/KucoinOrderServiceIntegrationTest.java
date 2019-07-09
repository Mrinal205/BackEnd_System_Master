package com.moonassist.service.exchange.kucoin;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.BaseTest;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.bind.kucoin.CoinInfo;
import com.moonassist.service.bind.kucoin.ExchangeInfo;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.exchange.ExchangeOrderServiceFactory;
import com.moonassist.service.exchange.binance.BinanceOrderService;
import com.moonassist.service.exchange.bittrex.BittrexOrderService;
import com.moonassist.service.exchange.coinbasepro.GDAXOrderService;
import com.moonassist.service.exchange.kucoin.KucoinOrderService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.moonassist.service.BaseTest.TEST_EXCHANGE.KUCOIN;

@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    ExchangeOrderServiceFactory.class,
    KucoinOrderService.class,
    ExchangeOrderService.class,
    BittrexOrderService.class,
    BinanceOrderService.class,
    GDAXOrderService.class,
})
@ActiveProfiles("kucoin")
public class KucoinOrderServiceIntegrationTest extends BaseTest {

  @Autowired
  private KucoinOrderService kucoinOrderService;

  @Autowired
  private ExchangeOrderServiceFactory exchangeOrderServiceFactory;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private RequestDetail requestDetail;

  @Test
  public void testExchangeInformation() {

    ExchangeInfo exchangeInfo = kucoinOrderService.exchangeInformation();

    Assert.assertNotNull(exchangeInfo);
    Assert.assertEquals(169, exchangeInfo.getData().size());
  }

  @Test
  public void testMassageAmount() {

    CoinInfo coinInfo = CoinInfo.builder()
        .coin("BTC")
        .tradePrecision(8)
        .build();

    BigDecimal result = kucoinOrderService.massageAmount(coinInfo, new BigDecimal("0.0471625699"));
    Assert.assertEquals(new BigDecimal("0.04716256"), result);
  }

  @Test
  public void testPlaceListAndCancelLimitOrder_KUCOIN_testPrecision() throws IOException, InterruptedException {

    TEST_EXCHANGE exchange = KUCOIN;
    Mockito.when(requestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.valueOf(exchange.name())));

    ExchangeOrderService exchangeOrderService = exchangeOrderServiceFactory.factory(requestDetail);

    Order order = Order.builder()
        .symbolPair("ETH/BTC")
        .orderType(OrderType.LIMIT)
        .offerType(OfferType.SELL)
        .price(new BigDecimal("0.08881345"))
        .amount(new BigDecimal("0.04716256"))
        .build();

    Order placedOrder = null;

    try {

      List<Order> orders = exchangeOrderService.findOpenOrders(requestDetail, order.getSymbolPair());
      Assert.assertTrue("Orders Not Empty for " + exchange + " orders:" + orders, orders.isEmpty());

      placedOrder = exchangeOrderService.placeOrder(requestDetail, order);

      //Wait for orders processing
      Thread.sleep(2000);

      orders = exchangeOrderService.findOpenOrders(requestDetail, order.getSymbolPair());
      Assert.assertNotNull(placedOrder);
      Assert.assertTrue( ! orders.isEmpty() );



    }
    finally {
      if (placedOrder != null) {
        boolean canceled = exchangeOrderService.cancelOrder(requestDetail, placedOrder.getExchangeOrderId(), order.getSymbolPair());
        Assert.assertTrue("Order Id: " + placedOrder.getExchangeOrderId() + " was not canceled", canceled);
      }
    }

  }

  @Test
  public void testGetClosedOrders() throws IOException {

    TEST_EXCHANGE exchange = KUCOIN;
    Mockito.when(requestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.valueOf(exchange.name())));

    ExchangeOrderService exchangeOrderService = exchangeOrderServiceFactory.factory(requestDetail);

    List<Order> orders = exchangeOrderService.findClosedOrders( requestDetail, "ETH/BTC");

    Assert.assertTrue( ! orders.isEmpty());
  }

}
