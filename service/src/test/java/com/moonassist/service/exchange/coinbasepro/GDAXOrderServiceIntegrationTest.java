package com.moonassist.service.exchange.coinbasepro;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.ExchangeTestUtil;
import com.moonassist.service.exchange.coinbasepro.GDAXOrderService;
import com.moonassist.service.exchange.order.OrderServiceTestBase;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GDAXOrderService.class})
public class GDAXOrderServiceIntegrationTest extends OrderServiceTestBase {

  @Autowired
  private GDAXOrderService gdaxOrderService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @Before
  public void setUp() {
    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(mockRequestDetail.getUserId()).thenReturn(userId);
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.GDAX));
    when(mockRequestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.GDAX.buildConnection()));
  }

  //No Funds available
  @Ignore
  @Test
  @SneakyThrows
  public void testGetOpenOrders() {

    Order order = gdaxOrderService.placeOrder(mockRequestDetail, ExchangeTestUtil.UNREASONABLE_LIMIT_ORDERS.get(ExchangeEnum.GDAX));

    List<Order> openOrders = gdaxOrderService.findOpenOrders(mockRequestDetail);

    Assert.assertNotNull(openOrders);
    Assert.assertFalse(openOrders.isEmpty());

    gdaxOrderService.cancelOrder(mockRequestDetail, order.getExchangeOrderId(), order.getSymbolPair());
  }

  @Ignore
  @Test
  public void testPlaceListAndCancelLimitOrder() {
    placeLimitOrder(gdaxOrderService);
  }


  /*********** Normally Ignored tests ********/

  @Ignore // No current orders
  @Test
  public void testGetAllOrders() {
    getAllOrders(gdaxOrderService, "ETH/USD");
  }

  @Ignore //Don't waste
  @Test
  public void testPlaceListAndCancelMarketOrder_GDAX_BUY() {
    placeMarketOrder(gdaxOrderService);
  }

  @Ignore
  @Test
  @SneakyThrows
  public void testPlaceListAndCancelMarketOrder_USDC() {

    Order order = Order.builder()
        .price(new BigDecimal("0.15"))
        .amount(new BigDecimal(10.543))
        .offerType(OfferType.BUY)
        .orderType(OrderType.LIMIT)
        .exchangeName(ExchangeEnum.GDAX)
        .symbolPair("BAT/USDC")
        .build();

    Order placedOrder = gdaxOrderService.placeOrder(mockRequestDetail, order);

    Assert.assertNotNull(placedOrder);

    gdaxOrderService.cancelOrder(mockRequestDetail, placedOrder.getExchangeOrderId(), placedOrder.getSymbolPair());
  }

  @Ignore //Don't waste
  @Test
  public void testPlaceListAndCancelMarketOrder_GDAX_SELL() {
    placeMarketOrder(gdaxOrderService, OfferType.SELL);
  }

  @Test
  public void testBuildLimit() {

    Order order = Order.builder()
        .symbolPair("ETH/USD")
        .amount(new BigDecimal("0.024748264"))
        .price(new BigDecimal("200.45"))
        .build();

    LimitOrder limitOrder = gdaxOrderService.buildLimit(mockRequestDetail, order);

    Assert.assertEquals(new BigDecimal("0.02"), limitOrder.getOriginalAmount());
  }

  @Test
  @SneakyThrows
  public void testGetOrderHistory_GDAX() {
    List<Order> orders = gdaxOrderService.findClosedOrders(mockRequestDetail,  "ETH/BTC");

    Assert.assertNotNull(orders);
    Assert.assertTrue( ! orders.isEmpty());
  }

}
