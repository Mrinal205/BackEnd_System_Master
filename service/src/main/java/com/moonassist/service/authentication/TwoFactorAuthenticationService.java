package com.moonassist.service.authentication;

import com.google.common.base.Preconditions;
import com.moonassist.persistence.user.*;
import com.moonassist.service.TimeService;

import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.UUID;

@Service
public class TwoFactorAuthenticationService {

  @Autowired
  private TwofactorRepository twofactorRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TimeBased2FA timeBased2FA;

  @Autowired
  private TimeService timeService;

  private static final String COMPANY = "Moon Assist";

  public String createNew2FA(Id<UserId> userId) {

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userId != null, "User with id : " + userId + "not found");

    Date now = timeService.now();
    String key = timeBased2FA.getRandomSecretKey();


    TwoFactorDTO twoFactorDTO = userDTO.getTwoFactorDTO();
    Preconditions.checkArgument(!isActive(twoFactorDTO), "Two factor already created and active");

    //TODO encrypt the key!!
    if (twoFactorDTO != null) {
      twoFactorDTO.setUpdated(now);
      twoFactorDTO.setConfirmed(false);
      twoFactorDTO.setKey(key);
    }
    else {
      twoFactorDTO = TwoFactorDTO.builder()
          .id(new Id<>())
          .created(now)
          .updated(now)
          .type(TwoFactoryType.TIME_BASED_ONE_TIME)
          .key(key)
          .user(userDTO)
          .confirmed(false)
          .build();
    }

    twofactorRepository.save(twoFactorDTO);

    return timeBased2FA.getGoogleAuthenticatorBarCode(key, userDTO.getEmail(), COMPANY);
  }

  @Transactional
  public void remove2FA(Id<UserId> userId, String number) {

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userId != null, "User with id : " + userId + "not found");
    Preconditions.checkArgument(validate(userId, number), "Invalid 2FA number");

    twofactorRepository.delete(userDTO.getTwoFactorDTO());
  }

  public boolean isActive(TwoFactorDTO twoFactorDTO) {
    return (twoFactorDTO != null && twoFactorDTO.getConfirmed());
  }


  public Boolean validate(Id<UserId> userId, String number) {

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userDTO != null, "User with id : " + userId + "not found");

    Preconditions.checkArgument(StringUtils.isNotEmpty(number), "number can not be empty");
    String conditionedNumber = number.replace(" ", "");

    TwoFactorDTO twoFactorDTO = twofactorRepository.findOneByUserId(userId);
    Preconditions.checkArgument(twoFactorDTO != null, "2FA not setup for userId: " + userId );

    String generatedNumber = timeBased2FA.getTOTPCode(twoFactorDTO.getKey());
    return generatedNumber.equals(conditionedNumber);
  }

  public Boolean requires2FA(Id<UserId> userId) {

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userId != null, "User with id : " + userId + "not found");

    return  (userDTO.getTwoFactorDTO() != null && userDTO.getTwoFactorDTO().getConfirmed());
  }

  public void confirm(Id<UserId> userId) {

    TwoFactorDTO twoFactorDTO = twofactorRepository.findOneByUserId(userId);
    Preconditions.checkArgument(twoFactorDTO != null, "2FA not setup for userId: " + userId );

    twoFactorDTO.setConfirmed(true);

    twofactorRepository.save(twoFactorDTO);
  }

}
