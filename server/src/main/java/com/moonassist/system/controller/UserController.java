package com.moonassist.system.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.moonassist.persistence.account.EmailSubscriptionDTO;
import com.moonassist.persistence.account.EmailSubscriptionType;
import com.moonassist.service.AuthenticationService;
import com.moonassist.service.EmailValidationService;
import com.moonassist.service.FraudService;
import com.moonassist.service.email.EmailSubscriptionService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.google.common.base.Preconditions;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.moonassist.bind.account.EmailSubscription;
import com.moonassist.bind.account.EmailSubscriptionEnum;
import com.moonassist.bind.authenticate.AuthenticateRequest;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.bind.authenticate.UserResponse;
import com.moonassist.bind.authenticate.VerifyEmail;
import com.moonassist.bind.user.ForgotPassword;
import com.moonassist.bind.user.Password;
import com.moonassist.bind.user.User;
import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserStatus;
import com.moonassist.service.AccountService;
import com.moonassist.service.DomainService;
import com.moonassist.service.EventsService;
import com.moonassist.service.UserService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.service.authentication.TwoFactorAuthenticationService;
import com.moonassist.service.email.VerifyEmailService;
import com.moonassist.system.security.SecurityConstants;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.WebUtils;

@Slf4j
@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

  @Autowired
  private UserService userService;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private EventsService eventsService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private TwoFactorAuthenticationService twoFactorAuthenticationService;

  @Autowired
  private VerifyEmailService verifyEmailService;

  @Autowired
  private DomainService domainService;

  @Autowired
  private EmailValidationService emailValidationService;

  @Autowired
  private EmailSubscriptionService emailSubscriptionService;

  @Autowired
  private FraudService fraudService;

  @Autowired
  private AuthenticationService authenticationService;

  private static final Long MAXIMUM_LOGIN_ATTEMPTS = 5L;

  //No Auth
  @ResponseBody
  @RequestMapping(method = RequestMethod.POST)
  ResponseEntity<UserResponse> create(@RequestBody User input, HttpServletRequest httpServletRequest) throws IOException, UnirestException {

    Preconditions.checkArgument(StringUtils.isNotEmpty(input.getPassword()), "password can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(input.getEmail()), "email can not be empty");

    fraudService.checkIPCache(httpServletRequest.getRemoteAddr(), MAXIMUM_LOGIN_ATTEMPTS);

    if ( ! emailValidationService.isApproved(input.getEmail()) ) {
      log.warn("Rejected unapproved email [" + input.getEmail() + "]");
    }
    Preconditions.checkArgument(emailValidationService.isApproved(input.getEmail()), "Invalid Email");

    UserDTO userDTO = userService.save(input.getEmail(), input.getName(), input.getPassword());
    verifyEmailService.send(userDTO.getId());
    AccountDTO accountDTO = accountService.getOrCreate(userDTO.getId());
    if (input.isNewsletter()) {
      accountService.saveEmailSubscription(accountDTO.getId(), EmailSubscription.builder()
          .type(EmailSubscriptionEnum.NEWSLETTER)
          .build());
    }

    UserResponse userResponse = UserResponse.builder()
        .userId(userDTO.getId().value())
        .build();

    //206 indicates needs to validate email
    return new ResponseEntity<>(userResponse, headers(), HttpStatus.PARTIAL_CONTENT);
  }

  //No Auth
  @ResponseBody
  @RequestMapping(value = "/validate", method = RequestMethod.POST)
  ResponseEntity<AuthenticateResponse> validate(@RequestBody VerifyEmail verifyEmail, HttpServletRequest httpServletRequest, HttpServletResponse response) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(verifyEmail.getUserId()), "UserId is required");
    Preconditions.checkArgument(StringUtils.isNotEmpty(verifyEmail.getCode()), "Code is required");
    fraudService.checkEmailCache(verifyEmail.getUserId(), MAXIMUM_LOGIN_ATTEMPTS);
    fraudService.checkIPCache(httpServletRequest.getRemoteAddr(), MAXIMUM_LOGIN_ATTEMPTS);

    //Raises exception if not valid
    userService.markValidated(Id.from(verifyEmail.getUserId()), verifyEmail.getCode());

    //Need this to forRequest account :-(
    UserDTO userDTO = userService.find(Id.from(verifyEmail.getUserId()));
    emailSubscriptionService.addSubscription(userDTO.getName(), userDTO.getEmail());
    
    AuthenticateResponse authenticateResponse = AuthenticateResponse.builder()
        .userId(userDTO.getId().value())
        .accountId(userDTO.getAccount().getId().value())
        .email(userDTO.getEmail())
        .build();

    String newToken = tokenService.createToken(userDTO.getId(), userDTO.getEmail());
    response.addCookie(SecurityConstants.createCookie(newToken, domainService.getBackendDomain()));

    return new ResponseEntity<>(authenticateResponse, headers(), HttpStatus.OK);
  }

  //No Auth
  @ResponseBody
  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest authenticateRequest, @RequestHeader("user-agent") String userAgent, HttpServletRequest request, HttpServletResponse response) {

    Preconditions.checkArgument(emailValidationService.isApproved(authenticateRequest.getEmail()), "Invalid Email");
    fraudService.checkEmailCache(authenticateRequest.getEmail(), MAXIMUM_LOGIN_ATTEMPTS);

    UserDTO userDTO = userService.find(authenticateRequest.getEmail(), authenticateRequest.getPassword());
    Preconditions.checkArgument(userDTO != null, "User not found");

    AccountDTO accountDTO = userDTO.getAccount() != null ?
        userDTO.getAccount() :
        accountService.getOrCreate(userDTO.getId());

    eventsService.login(userDTO.getId(), request.getRemoteAddr(), userAgent);

    if (userDTO.getStatus() == UserStatus.WAITING_VALIDATE) {
      return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    if (userDTO.getStatus() == UserStatus.SUSPENDED) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    //Check for required 2FA
    boolean requires2FA = twoFactorAuthenticationService.requires2FA(userDTO.getId());
    if (requires2FA) {
      AuthenticateResponse authenticateResponse = AuthenticateResponse.builder()
          .userId(userDTO.getId().value())
          .email(userDTO.getEmail())
          .build();

      return new ResponseEntity<>(authenticateResponse, headers(), HttpStatus.PARTIAL_CONTENT);
    }

    String newToken = tokenService.createToken(userDTO.getId(), userDTO.getEmail());
    AuthenticateResponse authenticated = AuthenticateResponse.builder()
        .userId(userDTO.getId().value())
        .email(userDTO.getEmail())
        .accountId(accountDTO.getId().value())
        .build();

    response.addCookie(SecurityConstants.createCookie(newToken, domainService.getBackendDomain()));

    return new ResponseEntity<>(authenticated, HttpStatus.OK);
  }

  //No Auth
  @ResponseBody
  @RequestMapping(value = "/authenticate", method = RequestMethod.DELETE)
  ResponseEntity<AuthenticateResponse> logout(HttpServletResponse response) {

    response.addCookie(SecurityConstants.logoutCookie(domainService.getBackendDomain()));
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(value = "/{id}/password", method = RequestMethod.PATCH)
  ResponseEntity<Void> updatePassword(@PathVariable("id") String id, @RequestBody Password passwordUpdate, HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException {

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);
    Preconditions.checkArgument(userId.equals(Id.from(id)), "Failure");

    userService.updatePassword(Id.from(id), passwordUpdate.getOldPassword(), passwordUpdate.getNewPassword());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  //TODO How do we prevent spamming this API?
  //NO AUTH
  @ResponseBody
  @RequestMapping(value = "/forgotpassword", method = RequestMethod.POST)
  ResponseEntity<Void> forgotPassword(@RequestBody ForgotPassword forgotPassword, HttpServletRequest httpServletRequest) throws IOException, UnirestException {

    Preconditions.checkArgument(emailValidationService.isApproved(forgotPassword.getEmail()), "Invalid Email");
    fraudService.checkIPCache(forgotPassword.getEmail(), MAXIMUM_LOGIN_ATTEMPTS);
    fraudService.checkIPCache(httpServletRequest.getRemoteAddr(), MAXIMUM_LOGIN_ATTEMPTS);

    userService.forgotPassword(forgotPassword.getEmail());

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(value = "/forgotpassword/verify", method = RequestMethod.POST)
  ResponseEntity<Void> forgotPasswordVerify(@RequestBody ForgotPassword forgotPassword, HttpServletRequest httpServletRequest) throws IOException, UnirestException {

    fraudService.checkIPCache(forgotPassword.getEmail(), MAXIMUM_LOGIN_ATTEMPTS);
    fraudService.checkIPCache(httpServletRequest.getRemoteAddr(), MAXIMUM_LOGIN_ATTEMPTS);

    userService.forgotPasswordVerify(forgotPassword.getEmail(), forgotPassword.getCode(), forgotPassword.getPassword());

    return new ResponseEntity<>(HttpStatus.OK);
  }

}
