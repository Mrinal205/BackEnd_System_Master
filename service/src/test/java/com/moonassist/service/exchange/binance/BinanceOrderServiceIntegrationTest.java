package com.moonassist.service.exchange.binance;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderStatus;
import com.moonassist.bind.order.OrderType;
import com.moonassist.persistence.order.OrderRepository;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.ExchangeTestUtil;
import com.moonassist.service.UserService;
import com.moonassist.service.bind.binance.ExchangeInfo;
import com.moonassist.service.bind.binance.Filter;
import com.moonassist.service.exception.OrderValidationException;
import com.moonassist.service.exchange.binance.BinanceOrderService;
import com.moonassist.service.exchange.order.OrderServiceTestBase;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@ActiveProfiles("binance")
@ContextConfiguration(classes = {BinanceOrderService.class, BinanceOrderConverter.class})
public class BinanceOrderServiceIntegrationTest extends OrderServiceTestBase {

  private String testingSymbolPair = "XRP/ETH";

  @Autowired
  private BinanceOrderService binanceOrderService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private OrderRepository orderRepository;

  @MockBean
  private UserService userService;


  @Before
  public void setUp() {
    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(mockRequestDetail.getUserId()).thenReturn(userId);
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BINANCE));
    when(mockRequestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.BINANCE.buildConnection()));
  }



  @Test
  public void testCLOSED_STATUS() {

    Assert.assertTrue(BinanceOrderService.CLOSED_STATUS.contains(org.knowm.xchange.dto.Order.OrderStatus.CANCELED));
    Assert.assertTrue(BinanceOrderService.CLOSED_STATUS.contains(org.knowm.xchange.dto.Order.OrderStatus.FILLED));
    Assert.assertTrue(BinanceOrderService.CLOSED_STATUS.contains(OrderStatus.CANCELED));
    Assert.assertTrue(BinanceOrderService.CLOSED_STATUS.contains(OrderStatus.TRADED));
  }

  @Ignore // Never have enough funds
  @Test
  public void testGetOpenOrders() throws IOException {
    getOpenOrders(binanceOrderService);
  }

  @Test
  public void testGetAllOrders() {
    getAllOrders(binanceOrderService, testingSymbolPair);
  }

  @Ignore //Flaky test
  @Test
  public void testPlaceListAndCancelLimitOrder() {
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BINANCE));
    placeLimitOrder(binanceOrderService);
  }

  @Test
  public void testGetAllOrders_market() throws IOException {

    List<Order> orders = binanceOrderService.findAllOrders( mockRequestDetail, "ETH/USDT");

    List<Order> marketOrders = orders.stream()
        .filter( order -> order.getOrderType() == OrderType.MARKET)
        .collect(Collectors.toList());

    Assert.assertFalse("No Market Orders found", marketOrders.isEmpty());

    for (Order order : orders) {

      Assert.assertTrue(order.getTimestamp() != null);
      Assert.assertNotNull(order.getPrice());

      if (order.getStatus() != OrderStatus.CANCELED) {
        Assert.assertTrue("Missing price [" + order + "]", order.getPrice().compareTo(BigDecimal.ZERO) > 0);
      }
    }
  }

  @Test
  @SneakyThrows
  public void testGetOrder() {

    Order order = binanceOrderService.getOrder(mockRequestDetail, "ETH/USDT", "69638125");

    Assert.assertNotNull(order);
    System.out.println(order);

  }


  @Test
  public void testGetOpenOrders_binance() throws IOException {

    List<Order> orders = binanceOrderService.findOpenOrders(mockRequestDetail, testingSymbolPair);

    Assert.assertNotNull(orders);
  }

  @Test
  public void testGetClosedOrders_binance() throws IOException {

    List<Order> orders = binanceOrderService.findClosedOrders( mockRequestDetail, "SNT/ETH");

    Assert.assertNotNull(orders);

    System.out.println(orders);
  }

  @Ignore
  @Test
  public void testPlaceLimitOrder() throws IOException {

    Order placeOrder = binanceOrderService.placeOrder(mockRequestDetail, ExchangeTestUtil.CRAZY_ORDER);

    binanceOrderService.cancelOrder(mockRequestDetail, placeOrder.getExchangeOrderId(), ExchangeTestUtil.CRAZY_ORDER.getSymbolPair());
  }

  //Order does not meet the minimal Price * Amount. Please increase your amount or price and try again
  @Test(expected = OrderValidationException.class)
  public void testPlaceLimitOrder_tooLow() throws IOException {

    Order order = Order.builder()
        .symbolPair("XRP/ETH")
        .orderType(OrderType.LIMIT)
        .offerType(OfferType.BUY)
        .price(new BigDecimal("0.0005"))
        .amount(new BigDecimal(1))
        .build();

    Order placeOrder = binanceOrderService.placeOrder(mockRequestDetail, order);

    binanceOrderService.cancelOrder(mockRequestDetail, placeOrder.getExchangeOrderId(), order.getSymbolPair());
  }

  @Test
  public void testExchangeInfo() {

    ExchangeInfo exchangeInfo = binanceOrderService.exchangeInformation();

    Assert.assertNotNull(exchangeInfo.symbolsMap().get("ETHBTC"));
    Assert.assertNotNull(exchangeInfo.symbolsMap().get("ETHBTC").filterMap().get(Filter.FilterType.LOT_SIZE));
    Assert.assertNotNull(exchangeInfo.symbolsMap().get("ETHBTC").filterMap().get(Filter.FilterType.LOT_SIZE).getStepSize());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarket_tooSmall() {

    Order order = Order.builder()
        .symbolPair("ETH/BTC")
        .orderType(OrderType.MARKET)
        .offerType(OfferType.BUY)
        .amount(new BigDecimal("0.00001"))
        .build();

    //Order Amount is smaller than minimal required: 0.00100000
    binanceOrderService.buildMarket(mockRequestDetail, order);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarket_tooLarge() {

    Order order = Order.builder()
        .symbolPair("ETH/BTC")
        .orderType(OrderType.MARKET)
        .offerType(OfferType.BUY)
        .amount(new BigDecimal("100001"))
        .build();

    //Order Amount is smaller than minimal required: 0.00100000
    binanceOrderService.buildMarket(mockRequestDetail, order);
  }

  @Test
  public void testMarket_rounded() {

    BigDecimal amount = new BigDecimal("3.12345");
    BigDecimal expected = new BigDecimal("3.123");

    Order order = Order.builder()
        .symbolPair("ETH/BTC")
        .orderType(OrderType.MARKET)
        .offerType(OfferType.BUY)
        .amount(amount)
        .build();

    //Order Amount is smaller than minimal required: 0.00100000
    MarketOrder marketOrder = binanceOrderService.buildMarket(mockRequestDetail, order);

    Assert.assertEquals(expected, marketOrder.getOriginalAmount());
  }

}
