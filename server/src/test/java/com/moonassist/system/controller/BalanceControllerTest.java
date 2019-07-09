package com.moonassist.system.controller;

import java.math.BigDecimal;

import com.moonassist.bind.balance.BalanceValues;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonassist.IntegrationTest;
import com.moonassist.TestingConstants;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Exchange;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.balance.Balance;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.TestUserHelper;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class BalanceControllerTest extends IntegrationTest {

  @LocalServerPort
  private int port;

  private TestRestTemplate testRestTemplate = new TestRestTemplate();

  private ObjectMapper objectMapper = new ObjectMapper();

  private String host;

  @Autowired
  private TestUserHelper testUserHelper;

  @Before
  public void setUp() {
    host = "http://localhost:" + port;
  }

  @Test
  public void testFetchBalance_binance() throws JsonProcessingException {


    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host);

    //Add Exchange
    Exchange exchange = Exchange.builder()
        .apiKey(TestingConstants.BINANCE_API_KEY)
        .secret(TestingConstants.BINANCE_SECRET_KEY)
        .exchangeName(ExchangeEnum.BINANCE)
        .build();

    HttpEntity<Exchange> httpEntity = request(exchange, token(response));

    ResponseEntity<Exchange> updateResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId() + "/exchanges", HttpMethod.POST, httpEntity, Exchange.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    //Verify Exchange is created correctly
    ResponseEntity<Account> getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue( ! getAccountResponse.getBody().getExchanges().isEmpty());

    HttpEntity<Void> voidEntity = voidRequest(token(response));
    ResponseEntity<Balance> balance = testRestTemplate.exchange(host + "/balances/BINANCE/ETH", HttpMethod.GET, voidEntity, Balance.class);

    Assert.assertEquals(HttpStatus.OK, balance.getStatusCode());

    BalanceValues balanceValues = balance.getBody().getValuesMap().get("ETH");

    Assert.assertTrue("Amount in exchange is not expected " + balanceValues.getAvailable(), balanceValues.getAvailable().compareTo(BigDecimal.ZERO) > 0);
  }

}
