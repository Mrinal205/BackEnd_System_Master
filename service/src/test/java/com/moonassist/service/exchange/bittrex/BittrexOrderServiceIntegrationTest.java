package com.moonassist.service.exchange.bittrex;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.ExchangeTestUtil;
import com.moonassist.service.exchange.bittrex.BittrexOrderService;
import com.moonassist.service.exchange.order.OrderServiceTestBase;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BittrexOrderService.class})
@ActiveProfiles("bittrex")
public class BittrexOrderServiceIntegrationTest extends OrderServiceTestBase {

  @Autowired
  private BittrexOrderService bittrexOrderService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;


  @Before
  public void setUp() {
    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(mockRequestDetail.getUserId()).thenReturn(userId);
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BITTREX));
    when(mockRequestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.BITTREX.buildConnection()));
  }

  @Test
  @SneakyThrows
  public void testGetOpenOrders() {

    Order placeOrder = bittrexOrderService.placeOrder(mockRequestDetail, ExchangeTestUtil.CRAZY_ORDER);

    //Bittrex takes a bit to realize the order
    Thread.sleep(1000);

    List<Order> openOrders = bittrexOrderService.findOpenOrders(mockRequestDetail);

    Assert.assertNotNull(openOrders);
    Assert.assertFalse(openOrders.isEmpty());

    bittrexOrderService.cancelOrder(mockRequestDetail, placeOrder.getExchangeOrderId(), placeOrder.getSymbolPair());
  }


  @Test
  public void testFindCorrectPriceForQuantity() {

    final BigDecimal buyResult = bittrexOrderService.findCorrectPriceForQuantity(mockRequestDetail, "ETH/USD", new BigDecimal("50"), OfferType.BUY);
    final BigDecimal sellResult = bittrexOrderService.findCorrectPriceForQuantity(mockRequestDetail, "ETH/USD", new BigDecimal("50"), OfferType.SELL);
    
    Assert.assertNotNull(buyResult);
    Assert.assertFalse( BigDecimal.ZERO.equals(buyResult) );
    Assert.assertNotEquals(buyResult, sellResult);
  }

}
