package com.moonassist.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonassist.IntegrationTest;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.authenticate.TwoFactor;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.service.authentication.TimeBased2FA;
import com.moonassist.system.security.WebSecurityConfig;

import com.moonassist.util.TestUserHelper;
import org.apache.commons.lang3.RandomStringUtils;
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
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class TwoFactorControllerTest extends IntegrationTest {

  @LocalServerPort
  private int port;

  private TestRestTemplate testRestTemplate = new TestRestTemplate();

  private ObjectMapper objectMapper = new ObjectMapper();

  private String host;

  private TimeBased2FA timeBased2FA = new TimeBased2FA();

  @Autowired
  private TestUserHelper testUserHelper;

  @Before
  public void setUp() {
    host = "http://localhost:" + port;
  }

  @Test
  public void test2FA() throws Exception {

    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "Eric Anderson", password);
    String authToken = token(response);

    TwoFactor twoFactor = TwoFactor.builder()
        .userId(response.getBody().getUserId())
        .type("time_based")
        .build();

    ResponseEntity<String> twoFactorResponse = testRestTemplate.exchange(host + "/2fa", HttpMethod.PUT, request(twoFactor, authToken), String.class);
    Assert.assertEquals(HttpStatus.OK, twoFactorResponse.getStatusCode());

    twoFactor = TwoFactor.builder()
        .userId(response.getBody().getUserId())
        .number(get2FANumberFromEmailBody(twoFactorResponse.getBody()))
        .build();

    ResponseEntity<Void> confirmResponse = testRestTemplate.exchange(host + "/2fa/confirm", HttpMethod.POST, request(twoFactor, authToken), Void.class);
    Assert.assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());


    //Now verify authentication works

    ResponseEntity<AuthenticateResponse> authResponse = testRestTemplate.exchange(host + "/2fa/authenticate", HttpMethod.POST, request(twoFactor), AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.OK, authResponse.getStatusCode());

  }

  @Test
  public void test2FA_delete() throws Exception {

    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "Eric Anderson", password);

    String authToken = token(response);

    TwoFactor twoFactor = TwoFactor.builder()
        .userId(response.getBody().getUserId())
        .type("time_based")
        .build();

    ResponseEntity<String> twoFactorResponse = testRestTemplate.exchange(host + "/2fa", HttpMethod.PUT, request(twoFactor, authToken), String.class);
    Assert.assertEquals(HttpStatus.OK, twoFactorResponse.getStatusCode());

    twoFactor = TwoFactor.builder()
        .userId(response.getBody().getUserId())
        .number(get2FANumberFromEmailBody(twoFactorResponse.getBody()))
        .build();

    ResponseEntity<Void> confirmResponse = testRestTemplate.exchange(host + "/2fa/confirm", HttpMethod.POST, request(twoFactor, authToken), Void.class);
    Assert.assertEquals(HttpStatus.OK, confirmResponse.getStatusCode());

    ResponseEntity<Void> deleteResponse = testRestTemplate.exchange(host + "/2fa", HttpMethod.DELETE, request(twoFactor, authToken), Void.class);
    Assert.assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
  }


  private String get2FANumberFromEmailBody(String body) {
    int start = body.indexOf("secret=");
    int stop = body.indexOf("&issuer=");
    String code = body.substring(start + 7, stop);
    return timeBased2FA.getTOTPCode(code);
  }

}
