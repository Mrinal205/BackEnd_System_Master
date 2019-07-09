package com.moonassist.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.moonassist.IntegrationTest;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Address;
import com.moonassist.bind.account.EmailSubscription;
import com.moonassist.bind.account.EmailSubscriptionEnum;
import com.moonassist.bind.account.Exchange;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.account.PersonalDetails;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.TestUserHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.util.Lists;
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

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class AccountControllerTest extends IntegrationTest {

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
  public void testAccountUpdate_name() throws Exception {

    String oldName = "Lars De Veer";
    String newName = "John Doe";
    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, oldName, password);

    //Verify Name is set correctly
    ResponseEntity<Account> getAccountResponse = testRestTemplate
        .exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, voidRequest(token(response)), Account.class);


    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertEquals(oldName, getAccountResponse.getBody().getPersonalDetails().getName());

    //Change name
    Account account = Account.builder()
        .address(Address.builder().build())
        .personalDetails(PersonalDetails.builder()
            .name(newName)
            .build())
        .build();

    HttpEntity<Account> httpEntity = request(account, token(response));
    ResponseEntity<String> updateResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.PUT, httpEntity, String.class);
    Assert.assertEquals(updateResponse.getBody(), HttpStatus.OK, updateResponse.getStatusCode());

    //Verify Name is updated correctly
    getAccountResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertEquals(newName, getAccountResponse.getBody().getPersonalDetails().getName());
  }

  @Test
  public void testAccountUpdate_exchange() throws Exception {

    String passphrase = RandomStringUtils.randomAlphabetic(10);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host);

    //Add Exchange
    Exchange exchange = Exchange.builder()
        .apiKey("someApiKey")
        .secret("someSecretKey")
        .exchangeName(ExchangeEnum.GDAX)
        .additional(ImmutableMap.of("passphrase", passphrase))
        .build();

    HttpEntity<Exchange> httpEntity = request(exchange, token(response));

    ResponseEntity<Exchange> updateResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId() + "/exchanges", HttpMethod.POST, httpEntity, Exchange.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    //Verify Exchange is created correctly
    ResponseEntity<Account> getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue( getAccountResponse.getBody().getExchanges().size() == 1);
    Assert.assertEquals(passphrase, Lists.newArrayList(getAccountResponse.getBody().getExchanges()).get(0).getAdditional().get("passphrase") );

    //Now Delete and Verify
    HttpEntity<Void> voidHttpEntity = voidRequest(token(response));
    String uri = host + "/accounts/" + response.getBody().getAccountId() + "/exchanges/" + getAccountResponse.getBody().getExchanges().iterator().next().getId();
    testRestTemplate.exchange(uri, HttpMethod.DELETE, voidHttpEntity, Void.class);

    getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue( getAccountResponse.getBody().getExchanges().isEmpty());
  }

  @Test
  public void testAccountUpdate_emailSubscription() throws Exception {

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host);

    //Add emailSubscription
    EmailSubscription emailSubscription = EmailSubscription.builder()
        .type(EmailSubscriptionEnum.LOGIN)
        .build();

    HttpEntity<EmailSubscription> httpEntity = request(emailSubscription, token(response));

    ResponseEntity<EmailSubscription> updateResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId() + "/emailsubscriptions", HttpMethod.POST, httpEntity, EmailSubscription.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());

    //Verify Email Subscription is updated correctly
    ResponseEntity<Account> getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue( ! getAccountResponse.getBody().getEmailSubscriptions().isEmpty());

    //Now Delete the Subscription
    HttpEntity<Void> voidHttpEntity = voidRequest(token(response));
    String uri = host + "/accounts/" + response.getBody().getAccountId() + "/emailsubscriptions/" + getAccountResponse.getBody().getEmailSubscriptions().iterator().next().getId();
    testRestTemplate.exchange(uri, HttpMethod.DELETE, voidHttpEntity, Void.class);

    //Verify Email Subscription is updated correctly
    getAccountResponse  = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId(), HttpMethod.GET, httpEntity, Account.class);
    Assert.assertEquals(HttpStatus.OK, getAccountResponse.getStatusCode());
    Assert.assertTrue(getAccountResponse.getBody().getEmailSubscriptions().isEmpty());
  }

}
