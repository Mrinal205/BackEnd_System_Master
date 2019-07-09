package com.moonassist.system.controller;

import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Preconditions;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.AccountService;
import com.moonassist.service.DomainService;
import com.moonassist.service.UserService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.service.authentication.TwoFactorAuthenticationService;
import com.moonassist.bind.authenticate.TwoFactor;
import com.moonassist.system.security.SecurityConstants;

import com.moonassist.type.Id;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/2fa")
public class TwoFactorController extends BaseController{

  private static final Logger LOGGER = LoggerFactory.getLogger(TwoFactorController.class);

  @Autowired
  private TwoFactorAuthenticationService twoFactorAuthenticationService;

  @Autowired
  private UserService userService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private TokenService tokenService;

  @Autowired
  private DomainService domainService;

  @ResponseBody
  @RequestMapping(method = RequestMethod.PUT)
  ResponseEntity<String> addTwoFactorAuthentication(@RequestBody TwoFactor twoFactor) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(twoFactor.getUserId()), "userId can not be empty");

    String barCode = twoFactorAuthenticationService.createNew2FA(Id.from(twoFactor.getUserId()));

    return new ResponseEntity<>(barCode, headers(), HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(value = "/confirm", method = RequestMethod.POST)
  ResponseEntity<Void> confirm(@RequestBody TwoFactor twoFactor) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(twoFactor.getUserId()), "userId can not be empty");

    boolean valid = twoFactorAuthenticationService.validate(new Id<>(twoFactor.getUserId()), twoFactor.getNumber());
    Preconditions.checkArgument(valid, "Invalid Number");

    twoFactorAuthenticationService.confirm(Id.from(twoFactor.getUserId()));

    return new ResponseEntity<>(headers(), HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(method = RequestMethod.DELETE)
  ResponseEntity<Void> removeTwoFactorAuthentication(@RequestBody TwoFactor twoFactor) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(twoFactor.getUserId()), "userId can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(twoFactor.getNumber()), "2FA number required");

    twoFactorAuthenticationService.remove2FA(Id.from(twoFactor.getUserId()), twoFactor.getNumber());

    return new ResponseEntity<>(headers(), HttpStatus.OK);
  }

  //No Auth Check
  @ResponseBody
  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
  ResponseEntity<AuthenticateResponse> authenticate(@RequestBody TwoFactor twoFactor, HttpServletResponse response) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(twoFactor.getUserId()), "userId can not be empty");

    boolean success = twoFactorAuthenticationService.validate(new Id<>(twoFactor.getUserId()), twoFactor.getNumber());
    Preconditions.checkArgument(success, "Invalid number");

    UserDTO userDTO = userService.find(new Id<>(twoFactor.getUserId()));
    Preconditions.checkArgument(userDTO != null, "User not found");

    //TODO add event for 2FA
    String newToken = tokenService.createToken(userDTO.getId(), userDTO.getEmail());
    AuthenticateResponse authenticated = AuthenticateResponse.builder()
        .userId(userDTO.getId().value())
        .email(userDTO.getEmail())
        .accountId(userDTO.getAccount().getId().value())
        .build();

    response.addCookie(SecurityConstants.createCookie(newToken, domainService.getBackendDomain()));

    return new ResponseEntity<>(authenticated, HttpStatus.OK);
  }

}
