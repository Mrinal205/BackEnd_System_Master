package com.moonassist.system.controller;

import com.moonassist.IntegrationTest;
import com.moonassist.TestingConstants;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Exchange;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderType;
import com.moonassist.bind.order.Orders;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.MockEmailService;
import com.moonassist.util.TestUserHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class OrderControllerTests extends IntegrationTest {

  @Autowired
  private MockEmailService mockEmailService;

  @LocalServerPort
  private int port;
  
  private String host;

  @Autowired
  private TestUserHelper testUserHelper;

  @Before
  public void setUp() {
    host = "http://localhost:" + port;
  }

  @Ignore //No Funds
  @Test
  public void createOrder() throws Exception {

    String symbolPair = "XRP/ETH";

    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "Eric Anderson", password);

    //Add Exchange
    Exchange exchange = Exchange.builder()
        .apiKey(TestingConstants.BINANCE_API_KEY)
        .secret(TestingConstants.BINANCE_SECRET_KEY)
        .exchangeName(ExchangeEnum.BINANCE)
        .build();

    HttpEntity<Exchange> httpEntity = request(exchange, token(response));

    ResponseEntity<Exchange> updateResponse = client().exchange(host + "/accounts/" + response.getBody().getAccountId() + "/exchanges", HttpMethod.POST, httpEntity, Exchange.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    ResponseEntity<Account> getAccountResponse  = client().exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue( ! getAccountResponse.getBody().getExchanges().isEmpty());

    Order order = Order.builder()
        .symbolPair(symbolPair)
        .orderType(OrderType.LIMIT)
        .amount(new BigDecimal(20))
        .exchangeName(ExchangeEnum.BINANCE)
        .price(new BigDecimal("0.0005"))
        .exchangeName(ExchangeEnum.BINANCE)
        .offerType(OfferType.BUY)
        .build();

    HttpEntity<Order> orderHttpEntity = request(order, token(response));
    ResponseEntity<Order> orderResponse = client().exchange(host + "/orders", HttpMethod.POST, orderHttpEntity, Order.class);
    Assert.assertEquals(HttpStatus.OK, orderResponse.getStatusCode());


    HttpEntity<Void> ordersHttpEntity = voidRequest(token(response));
    ResponseEntity<Orders> ordersResponse =
        client().exchange(host + "/orders/" + ExchangeEnum.BINANCE + "/" + symbolPair.replace("/", "-"), HttpMethod.GET, ordersHttpEntity, Orders.class);
    Assert.assertEquals(HttpStatus.OK, ordersResponse.getStatusCode());
    Assert.assertTrue( ! ordersResponse.getBody().getOpen().isEmpty() );


    HttpEntity<Void> deleteOrderHttpEntity = voidRequest(token(response));
    ResponseEntity<Void> deleteRequest = client().exchange(host + "/orders/" + orderResponse.getBody().getId(), HttpMethod.DELETE, deleteOrderHttpEntity, Void.class);
    Assert.assertEquals(HttpStatus.NO_CONTENT, deleteRequest.getStatusCode());
  }

  @Test
  public void createOrder_noExchange() throws Exception {

    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "Eric Anderson", password);

    Order order = Order.builder()
        .symbolPair("BTC/USD")
        .orderType(OrderType.LIMIT)
        .amount(new BigDecimal(0.0001))
        .exchangeName(ExchangeEnum.GDAX)
        .price(new BigDecimal(1000.99))
        .offerType(OfferType.BUY)
        .build();

    HttpEntity<Order> httpEntity = request(order, token(response));
    ResponseEntity<String> orderResponse = client().exchange(host + "/orders", HttpMethod.POST, httpEntity, String.class);


    Assert.assertEquals(orderResponse.getBody(), HttpStatus.BAD_REQUEST, orderResponse.getStatusCode());
  }

}