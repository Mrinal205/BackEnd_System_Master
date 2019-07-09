package com.moonassist.service.authentication;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.moonassist.exception.NotFoundException;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.EncryptionService;
import com.moonassist.service.TimeService;
import com.moonassist.service.UserService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

import lombok.SneakyThrows;

@Service
public class TokenService implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenService.class);

  private Key aesKey;

  @Value("${encryption.authentication.key}")
  private String key;

  private static int TIMEOUT_MINUTES = 60;

  private EncryptionService encryptionService;

  private TimeService timeService;

  private UserService userService;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final String DATE_FORMAT = "MM/DD hh:mm";

  @Autowired
  public TokenService(EncryptionService encryptionService, TimeService timeService, UserService userService) {
    this.encryptionService = encryptionService;
    this.timeService = timeService;
    this.userService = userService;
  }

  public void init() {
    Preconditions.checkState(StringUtils.isNotEmpty(key), "Found an empty key");
    aesKey = EncryptionService.generate(key);
  }

  @SneakyThrows
  public String createToken(Id<UserId> userId, String email) {

      UserDTO userDTO = userService.find(userId);
      NotFoundException.check(userDTO != null, "User not found");

      AuthenticationToken authenticationToken = AuthenticationToken.builder()
          .userId(userId.value())
          .email(email)
          .passwordHash(userDTO.getPasswordHash())
          .lastRequest(timeService.now())
          .build();

      String json = OBJECT_MAPPER.writeValueAsString(authenticationToken);

      return encryptionService.encrypt(json, aesKey);
  }

  public boolean validate(String token) {

    try {
      AuthenticationToken authenticationToken = decrypt(token);
      Date compareDate = DateUtils.addMinutes(authenticationToken.getLastRequest(), TIMEOUT_MINUTES);

      UserDTO userDTO =userService.find(Id.from(authenticationToken.getUserId()));
      NotFoundException.check(userDTO != null, "User not found");

      if ( ! userDTO.getPasswordHash().equals(authenticationToken.getPasswordHash())) {
        // Not sure which exception to throw
        LOGGER.info("Invalid passwordHash found for " + userDTO.getId());
        return false;
      }

      LOGGER.debug("Comparing Last Request[" + DateFormatUtils.format(compareDate, DATE_FORMAT) + "] > NOW [" + DateFormatUtils.format(timeService.now(), DATE_FORMAT));

      return (compareDate.compareTo(timeService.now()) > 0);
    } catch (Exception e) {
      LOGGER.warn("Invalid token");
      return false;
    }

  }

  @SneakyThrows
  public String refresh(String token) {
      AuthenticationToken authenticationToken = decrypt(token);
      return createToken(Id.from(authenticationToken.getUserId()), authenticationToken.getEmail());
  }

  @SneakyThrows
  public AuthenticationToken decrypt(String encryptedToken) {
      String json = encryptionService.decrypt(encryptedToken, aesKey);
      return OBJECT_MAPPER.readValue(json, AuthenticationToken.class);
  }


  @Override
  public void afterPropertiesSet() {
    init();
  }

}
