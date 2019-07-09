package com.moonassist.system.controller;

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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moonassist.IntegrationTest;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.authenticate.AuthenticateRequest;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.authenticate.UserResponse;
import com.moonassist.bind.authenticate.VerifyEmail;
import com.moonassist.bind.user.ForgotPassword;
import com.moonassist.bind.user.Password;
import com.moonassist.bind.user.User;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.MockEmailService;
import com.moonassist.util.TestUserHelper;

@RunWith(SpringRunner.class)
@SpringBootTest(
		classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan
public class UserControllerTests extends IntegrationTest {

  @Autowired
  private MockEmailService mockEmailService;

	@LocalServerPort
	private int port;

	private TestRestTemplate testRestTemplate = new TestRestTemplate();

	private String host;

	@Autowired
  private TestUserHelper testUserHelper;

	@Before
  public void setUp() {
    host = "http://localhost:" + port;
  }

	@Test
	public void testCreateUser() {

    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
    String password = RandomStringUtils.randomAlphabetic(16);

	  User user = User.builder()
        .email(email)
        .name("Eric Anderson")
        .password(password)
        .build();

    ResponseEntity<UserResponse> response = testRestTemplate.postForEntity(host + "/users", request(user), UserResponse.class);

    Assert.assertEquals(HttpStatus.PARTIAL_CONTENT, response.getStatusCode());
	}

  @Test
  public void testCreateUser_missingEmail() {

    User user = User.builder()
        .name("Eric Anderson")
        .password("password")
        .build();

    ResponseEntity<String> response = testRestTemplate.postForEntity(host + "/users", request(user), String.class);

    Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    Assert.assertTrue(response.getBody().contains("email can not be empty"));
  }


	@Test
	public void testSimpleAuthenticationWorkflow() {


	  // Create user
		String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";
		String password = RandomStringUtils.randomAlphabetic(16);

		User user = User.builder()
				.email(email)
				.name("Eric Anderson")
				.password(password)
				.build();

		ResponseEntity<UserResponse> userCreateResponse = testRestTemplate.postForEntity(host + "/users", request(user), UserResponse.class);
    Assert.assertEquals(HttpStatus.PARTIAL_CONTENT, userCreateResponse.getStatusCode());

		//Validate email
		VerifyEmail verifyEmailRequest = VerifyEmail.builder()
				.userId(userCreateResponse.getBody().getUserId())
        .code(mockEmailService.getLastCode())
				.build();

		ResponseEntity<AuthenticateResponse> validateResponse = testRestTemplate.postForEntity(host + "/users/validate", verifyEmailRequest, AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.OK, validateResponse.getStatusCode());


		// Test Authentication
		AuthenticateRequest authenticateRequest = AuthenticateRequest.builder()
				.email(email)
				.password(password)
				.build();
    ResponseEntity<AuthenticateResponse> loginResponse = testRestTemplate.postForEntity( host + "/users/authenticate", request(authenticateRequest), AuthenticateResponse.class);

		Assert.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    assertAuthCookie(loginResponse);

		//Verify Account Login History
    HttpEntity<Void> request = voidRequest(token(loginResponse));
    ResponseEntity<Account> accountResponse = testRestTemplate.exchange(host + "/accounts/" + loginResponse.getBody().getAccountId(), HttpMethod.GET, request, Account.class);

    Assert.assertEquals(HttpStatus.OK, accountResponse.getStatusCode());
    Assert.assertEquals("127.0.0.1", accountResponse.getBody().getLoginEvents().get(0).getIpAddress());
    Assert.assertEquals("Chrome, macOS", accountResponse.getBody().getLoginEvents().get(0).getUserAgent());


    //Logout
    ResponseEntity<Void> deleteResponse = testRestTemplate.exchange( host + "/users/authenticate", HttpMethod.DELETE, request(Void.class), Void.class);
    Assert.assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
  }

  @Test
  public void testPasswordChange() throws JsonProcessingException {

	  String oldPassword = RandomStringUtils.randomAlphabetic(22);
	  String newPassword = RandomStringUtils.randomAlphabetic(24);

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, oldPassword);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

    //Change Password
    Password passwordChangeRequest = Password.builder()
        .oldPassword(oldPassword)
        .newPassword(newPassword)
        .build();

    String uri = host + "/users/" + response.getBody().getUserId() + "/password";
    ResponseEntity<Void> updateResponse = testRestTemplate.exchange(uri , HttpMethod.PATCH, request(passwordChangeRequest, token(response)), Void.class);
    Assert.assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
    
    // Test Authentication with OLD password
    AuthenticateRequest authenticateRequest = AuthenticateRequest.builder()
        .email(response.getBody().getEmail())
        .password(oldPassword)
        .build();
    ResponseEntity<AuthenticateResponse> loginResponse = testRestTemplate.postForEntity( host + "/users/authenticate", request(authenticateRequest), AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.BAD_REQUEST, loginResponse.getStatusCode());


    // Test Authentication with NEW password
    authenticateRequest = AuthenticateRequest.builder()
        .email(response.getBody().getEmail())
        .password(newPassword)
        .build();
    loginResponse = testRestTemplate.postForEntity( host + "/users/authenticate", request(authenticateRequest), AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    assertAuthCookie(loginResponse);

  }

  @Test
  public void testForgotPassword() throws JsonProcessingException {

    String password = RandomStringUtils.randomAlphabetic(21);
    String newPassword = RandomStringUtils.randomAlphabetic(21);
    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "name", password);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());


    ForgotPassword forgotPassword = ForgotPassword.builder()
        .email(email)
        .build();

    ResponseEntity<Void> forgotEmailResponse = testRestTemplate.postForEntity( host + "/users/forgotpassword", request(forgotPassword), Void.class);
    Assert.assertEquals(HttpStatus.OK, forgotEmailResponse.getStatusCode());

    forgotPassword = ForgotPassword.builder()
        .email(email)
        .code(mockEmailService.getLastCode())
        .password(newPassword)
        .build();

    forgotEmailResponse = testRestTemplate.postForEntity( host + "/users/forgotpassword/verify", request(forgotPassword), Void.class);
    Assert.assertEquals(HttpStatus.OK, forgotEmailResponse.getStatusCode());

    // Test Authentication with NEW password
    AuthenticateRequest authenticateRequest = AuthenticateRequest.builder()
        .email(email)
        .password(newPassword)
        .build();

    ResponseEntity<AuthenticateResponse> loginResponse = testRestTemplate.postForEntity( host + "/users/authenticate", request(authenticateRequest), AuthenticateResponse.class);
    Assert.assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    assertAuthCookie(loginResponse);
  }


  @Test
  public void testForgotPassword_invalidCode() throws JsonProcessingException {

    String password = RandomStringUtils.randomAlphabetic(21);
    String newPassword = RandomStringUtils.randomAlphabetic(21);
    String email = RandomStringUtils.randomAlphabetic(10) + "-test@moonassist.com";

    ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host, email, "name", password);
    Assert.assertEquals(HttpStatus.OK, response.getStatusCode());


    ForgotPassword forgotPassword = ForgotPassword.builder()
        .email(email)
        .build();

    ResponseEntity<Void> forgotEmailResponse = testRestTemplate.postForEntity( host + "/users/forgotpassword", request(forgotPassword), Void.class);
    Assert.assertEquals(HttpStatus.OK, forgotEmailResponse.getStatusCode());

    forgotPassword = ForgotPassword.builder()
        .email(email)
        .code("WRONG_CODE")
        .password(newPassword)
        .build();

    ResponseEntity<String> badResponse = testRestTemplate.postForEntity( host + "/users/forgotpassword/verify", request(forgotPassword), String.class);
    Assert.assertEquals(HttpStatus.BAD_REQUEST, badResponse.getStatusCode());
    Assert.assertTrue(badResponse.getBody().contains("\"message\":\"Invalid reset code\""));
  }

}