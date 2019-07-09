package com.moonassist.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonassist.bind.user.User;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.authenticate.VerifyEmail;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TestUserHelper {

  @Autowired
  private MockEmailService mockEmailService;

  private TestRestTemplate testRestTemplate = new TestRestTemplate();

  private ObjectMapper objectMapper = new ObjectMapper();

  public ResponseEntity<AuthenticateResponse> createUser(String host, String email, String name, String password) throws JsonProcessingException {

    //Create User
    User user = User.builder()
        .email(email)
        .name(name)
        .password(password)
        .build();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(user), headers);

    ResponseEntity<AuthenticateResponse> userCreateResponse = testRestTemplate.postForEntity(host + "/users", request, AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.PARTIAL_CONTENT, userCreateResponse.getStatusCode());

    //Validate email
    VerifyEmail verifyEmailRequest = VerifyEmail.builder()
        .userId(userCreateResponse.getBody().getUserId())
        .code(mockEmailService.getLastCode())
        .build();

    ResponseEntity<AuthenticateResponse> validateResponse = testRestTemplate.postForEntity(host + "/users/validate", verifyEmailRequest, AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.OK, validateResponse.getStatusCode());

    return validateResponse;
  }

  public ResponseEntity<AuthenticateResponse> createUser(String host, String password) throws JsonProcessingException {

    String name = "Test User";
    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    return createUser(host, email, name, password);
  }

  public ResponseEntity<AuthenticateResponse> createUser(String host) throws JsonProcessingException {

    String password = RandomStringUtils.randomAlphabetic(16);
    return createUser(host, password);
  }

}