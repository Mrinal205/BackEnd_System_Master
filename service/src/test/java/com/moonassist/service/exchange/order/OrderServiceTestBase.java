package com.moonassist.service.exchange.order;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;
import com.moonassist.service.BaseTest;
import com.moonassist.service.ExchangeTestUtil;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.ExchangeOrderService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.List;

public class OrderServiceTestBase extends BaseTest {

  @MockBean
  protected RequestDetail mockRequestDetail;


  @SneakyThrows
  public void getOpenOrders(final ExchangeOrderService exchangeOrderService) {

    Order placeOrder = exchangeOrderService.placeOrder(mockRequestDetail, ExchangeTestUtil.CRAZY_ORDER);

    List<Order> openOrders = exchangeOrderService.findOpenOrders(mockRequestDetail);

    Assert.assertNotNull(openOrders);
    Assert.assertFalse(openOrders.isEmpty());

    exchangeOrderService.cancelOrder(mockRequestDetail, placeOrder.getExchangeOrderId(), placeOrder.getSymbolPair());
  }

  @SneakyThrows
  public void getAllOrders(final ExchangeOrderService exchangeOrderService, String symbolPair) {

    List<Order> orders = exchangeOrderService.findAllOrders(mockRequestDetail, symbolPair);

    Assert.assertNotNull(orders);
    Assert.assertFalse(orders.isEmpty());

    for (Order order : orders) {

      Assert.assertTrue(order.getTimestamp() != null);
      Assert.assertNotNull(order.getPrice());
      Assert.assertTrue(order.getPrice().compareTo(BigDecimal.ZERO) > 0);

    }

  }


  @SneakyThrows
  public void placeLimitOrder(final ExchangeOrderService exchangeOrderService) {

    Order order = ExchangeTestUtil.UNREASONABLE_LIMIT_ORDERS.get(ExchangeEnum.valueOf(mockRequestDetail.getExchangeEnum().get().name()));
    Order placedOrder = null;

    try {

      List<Order> orders = exchangeOrderService.findOpenOrders(mockRequestDetail, order.getSymbolPair());
      Assert.assertTrue("Orders Not Empty for " + mockRequestDetail.getExchangeEnum().get() + " orders:" + orders, orders.isEmpty());

      placedOrder = exchangeOrderService.placeOrder(mockRequestDetail, order);

      //Wait for orders processing
      Thread.sleep(2000);

      orders = exchangeOrderService.findOpenOrders(mockRequestDetail, order.getSymbolPair());
      Assert.assertNotNull(placedOrder);
      Assert.assertTrue(!orders.isEmpty());
    } finally {
      if (placedOrder != null) {
        boolean canceled = exchangeOrderService.cancelOrder(mockRequestDetail, placedOrder.getExchangeOrderId(), order.getSymbolPair());
        Assert.assertTrue("Order Id: " + placedOrder.getExchangeOrderId() + " was not canceled", canceled);
      }
    }

  }


  public void placeMarketOrder(final ExchangeOrderService exchangeOrderService) {
    placeMarketOrder(exchangeOrderService, OfferType.BUY);
  }

  public void placeMarketOrder(final ExchangeOrderService exchangeOrderService, final OfferType offerType) {

    Order order = ExchangeTestUtil.SMALL_REASONABLE_MARKET_ORDERS.get(ExchangeEnum.valueOf(mockRequestDetail.getExchangeEnum().get().name()));
    order.setOrderType(OrderType.MARKET);
    order.setOfferType(offerType);

    Order placedOrder = exchangeOrderService.placeOrder(mockRequestDetail, order);

    Assert.assertNotNull(placedOrder);
  }

  @SneakyThrows
  public void cancelOrder(ExchangeOrderService exchangeOrderService, CurrencyPair currencyPair) {

    Currency base = (currencyPair.base != new Currency("BCC")) ?
        currencyPair.base :
        new Currency("BCH");

    List<Order> orders = exchangeOrderService.findOpenOrders(mockRequestDetail, base + "/" + currencyPair.counter);

    for (Order order : orders) {
      boolean canceled = exchangeOrderService.cancelOrder(mockRequestDetail, order.getExchangeOrderId(), order.getSymbolPair());
      Assert.assertTrue("Order Id: " + order.getExchangeOrderId() + " was not canceled", canceled);
    }

    if (!orders.isEmpty()) {
      System.out.println("Canceled " + orders.size() + " orders");
    }

  }

}
