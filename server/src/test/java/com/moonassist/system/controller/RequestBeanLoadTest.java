package com.moonassist.system.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.moonassist.IntegrationTest;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Exchange;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.TestUserHelper;

import lombok.Builder;
import lombok.Getter;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class RequestBeanLoadTest extends IntegrationTest {

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

  ExecutorService executor = Executors.newFixedThreadPool(3);


  @Test
  public void testExchangeConnection() throws Exception {


    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host);

    //Add Exchange
    Exchange exchange = Exchange.builder()
        .apiKey("f862ba40922a34aa8b8089ba658b93f6")
        .secret("zp1d92jyj2OuXpqu3I0CIkcLmn4vLJYupOWm/NQV/TZH1BXm8cJZ0AmTKn9iEFy+Rp9OhqiBkMdLMF9BztxTeQ==")
        .exchangeName(ExchangeEnum.GDAX)
        .additional(ImmutableMap.of("passphrase", "wywftsucam"))
        .build();
    createExchange(response, exchange);


    exchange = Exchange.builder()
        .apiKey("2LMBtuKyBN18khoJAQ8ib4xLVEFSCQN0aWDQW61AdZtxR9m4trEK4ubEWysRucWx")
        .secret("3UbPVQxl3HBHmGS31MFGaq60KNb3Y1ZK9oZROMPkA8BcjX4PGpUEFpAbg9bwZeeB")
        .exchangeName(ExchangeEnum.BINANCE)
        .build();
    createExchange(response, exchange);

    exchange = Exchange.builder()
        .apiKey("d60b8a9d13664541ac62c957a7c355ea")
        .secret("99feda1984fb48a8a0ec7d9706d72763")
        .exchangeName(ExchangeEnum.BITTREX)
        .build();
    createExchange(response, exchange);

    HttpEntity<Void> httpEntity = voidRequest(token(response));


    List<TestConnectionTask> tasks = new ArrayList<>();
    for (int i = 0; i < 5; i++) {

      tasks.add(TestConnectionTask.builder()
          .exchange("GDAX")
          .host(host)
          .httpEntity(httpEntity)
          .build());

      tasks.add(TestConnectionTask.builder()
          .exchange("BITTREX")
          .host(host)
          .httpEntity(httpEntity)
          .build());

      tasks.add(TestConnectionTask.builder()
          .exchange("BINANCE")
          .host(host)
          .httpEntity(httpEntity)
          .build());

    }

    List<Future<TestConnectionResult>> futures = executor.invokeAll(tasks);

    futures.forEach( future -> {
          try {
            Assert.assertEquals(future.get().body, HttpStatus.OK, future.get().httpStatus);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
    );





//    ResponseEntity<String> testResponse  = testRestTemplate.exchange(host + "/test/connection?exchange=GDAX", HttpMethod.GET, httpEntity, String.class);
//    Assert.assertEquals(testResponse.getBody(), HttpStatus.OK, testResponse.getStatusCode());
//
//    testResponse  = testRestTemplate.exchange(host + "/test/connection?exchange=BINANCE", HttpMethod.GET, httpEntity, String.class);
//    Assert.assertEquals(testResponse.getBody(), HttpStatus.OK, testResponse.getStatusCode());
//
//    testResponse  = testRestTemplate.exchange(host + "/test/connection?exchange=BITTREX", HttpMethod.GET, httpEntity, String.class);
//    Assert.assertEquals(testResponse.getBody(), HttpStatus.OK, testResponse.getStatusCode());


  }

  private void createExchange(final ResponseEntity<AuthenticateResponse> response, final Exchange exchange) {

    HttpEntity<Exchange> httpEntity = request(exchange, token(response));

    ResponseEntity<Exchange> updateResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId() + "/exchanges", HttpMethod.POST, httpEntity, Exchange.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    //Verify Exchange is created correctly
    ResponseEntity<Account> getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
//    Assert.assertTrue( getAccountResponse.getBody().getExchanges().size() == 1);
//    Assert.assertEquals(passphrase, Lists.newArrayList(getAccountResponse.getBody().getExchanges()).get(0).getAdditional().get("passphrase") );


  }


  @Builder
  @Getter
  public static final class TestConnectionResult {

    private HttpStatus httpStatus;
    private String body;

  }

  @Builder
  public static final class TestConnectionTask implements Callable<TestConnectionResult> {

    private HttpEntity<Void> httpEntity;
    private String host;
    private String exchange;


    @Override
    public TestConnectionResult call() {

      TestRestTemplate testRestTemplate = new TestRestTemplate();

//      ResponseEntity<String> testResponse  = testRestTemplate.exchange(host + "/test/connection?exchange=" + exchange, HttpMethod.GET, httpEntity, String.class);

      ResponseEntity<String> testResponse  = testRestTemplate.exchange(host + "orders/ " + exchange  + "?status=OPEN", HttpMethod.GET, httpEntity, String.class);


      return TestConnectionResult.builder()
          .body(testResponse.getBody())
          .httpStatus(testResponse.getStatusCode())
          .build();
    }
  }










}
