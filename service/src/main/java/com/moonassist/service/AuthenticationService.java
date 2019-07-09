package com.moonassist.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.Account;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.authentication.AuthenticationToken;
import com.moonassist.service.authentication.TokenService;

@Service
public class AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);


  @Autowired
  private TokenService tokenService;


  /**
   * Validates the token and returns the UserId logged in
   * @param token
   * @return UserId of token
   * @throws AuthenticationException
   */
  public Id<UserId> retrieveAuthentication(String token) throws AuthenticationException {

    try {
      AuthenticationToken authenticationToken = tokenService.decrypt(token);
      Preconditions.checkArgument(StringUtils.isNotEmpty(authenticationToken.getUserId()), "UserId is invalid");
      return Id.from(authenticationToken.getUserId());

  } catch (Exception e) {
      LOGGER.error("Error authenticating");
      throw new AuthenticationException("Auth", e);
    }
  }

  public static final class AuthenticationException extends Exception {

    public AuthenticationException(String message, Exception exception) {
      super(message, exception);
    }
  }

}
