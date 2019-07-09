package com.moonassist.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.moonassist.exception.ConflictException;
import com.moonassist.exception.NotFoundException;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.persistence.user.UserStatus;
import com.moonassist.service.authentication.UpdatableBCrypt;
import com.moonassist.service.email.ForgotPasswordEmailService;

import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

  private static final UpdatableBCrypt bcrypt = new UpdatableBCrypt(11);

  private UserRepository userRepository;

  private ForgotPasswordEmailService forgotPasswordEmailService;

  private TimeService timeService;

  private RandomString codeGenerator = new RandomString(VERIFY_CODE_LENGTH);

  public static final Integer VERIFY_CODE_LENGTH = 42;

  public static final Integer FORGOT_PASSWORD_EXPIRE = 30; //MINUTES

  @Autowired
  public UserService(UserRepository userRepository, ForgotPasswordEmailService forgotPasswordEmailService, TimeService timeService) {
    this.userRepository = userRepository;
    this.forgotPasswordEmailService = forgotPasswordEmailService;
    this.timeService = timeService;
  }

  /**
   *
   * @param email
   * @param password
   * @return
   * @throws IllegalArgumentException if user not found or password is inValid
   */
  @Transactional
  public UserDTO find(String email, String password) {

    UserDTO userDTO = userRepository.findOneByEmail(email);
    Preconditions.checkArgument(userDTO != null, "No user found for email: " + email);

    boolean isValid = bcrypt.verifyHash(password, userDTO.getPasswordHash());
    Preconditions.checkArgument(isValid , "Invalid password");

    return userDTO;
  }

  public UserDTO find(Id<UserId> id) {

    UserDTO userDTO = userRepository.findOne(id);
    NotFoundException.check(userDTO != null, "User not found");

    return userDTO;
  }

  public Set<UserDTO> findActive() {

    return userRepository.findAllByStatus(UserStatus.ACTIVE);
  }

  public UserDTO save(String email, String name, String password) {

    UserDTO userDTO = UserDTO.builder()
        .id(new Id<>())
        .email(email)
        .name(name)
        .passwordHash(bcrypt.hash(password))
        .passwordhashDate(new Date())
        .passwordHashVersion("0.1")
        .status(UserStatus.WAITING_VALIDATE)
        .validateCode(codeGenerator.nextString())
        .build();

    LOGGER.info("Saving " + email);
    try {
      return userRepository.save(userDTO);
    } catch (DataIntegrityViolationException e) {
      throw new ConflictException("Duplicate User", e);
    }
  }

  public void markValidated(Id<UserId> userId, String code) {

    Preconditions.checkArgument(userId != null, "userId can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(code), "code can not be empty");

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userDTO != null, "User not found");
    Preconditions.checkArgument(userDTO.getValidateCode().equals(code.trim()), "Invalid code");

    userDTO.setStatus(UserStatus.ACTIVE);
    userRepository.save(userDTO);
  }

  public void updatePassword(Id<UserId> id, String oldPassword, String newPassword) {

    UserDTO userDTO = userRepository.findOne(id);
    Preconditions.checkArgument(userDTO != null, "User not found");
    boolean isValid = bcrypt.verifyHash(oldPassword, userDTO.getPasswordHash());
    Preconditions.checkArgument(isValid , "Invalid old password");

    userDTO.setPasswordHash(bcrypt.hash(newPassword));
    userRepository.save(userDTO);
  }

  public void forgotPassword(String email) throws IOException, UnirestException {

    Preconditions.checkArgument(StringUtils.isNoneEmpty(email), "email can not be empty");
    UserDTO userDTO = userRepository.findOneByEmail(email);
    Preconditions.checkArgument(userDTO != null, "No user found for email: " + email);


    String forgotPasswordCode = codeGenerator.nextString();
    userDTO.setForgotEmailCode(forgotPasswordCode);
    userDTO.setForgotEmailcreated(new Date());
    userRepository.save(userDTO);

    forgotPasswordEmailService.send(userDTO.getEmail(), forgotPasswordCode);
  }

  public void forgotPasswordVerify(String email, String code, String password) throws IOException, UnirestException {

    Preconditions.checkArgument(StringUtils.isNoneEmpty(email), "email can not be empty");
    UserDTO userDTO = userRepository.findOneByEmail(email);
    Preconditions.checkArgument(userDTO != null, "No user found for email: " + email);
    Preconditions.checkArgument(StringUtils.isNotEmpty(userDTO.getForgotEmailCode()), "User invalid code");
    Preconditions.checkArgument(StringUtils.isNotEmpty(code), "code can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(password), "password can not be empty");

    //Validate the code is correct
    Preconditions.checkArgument(userDTO.getForgotEmailCode().equals(code.trim()), "Invalid reset code");
    Preconditions.checkArgument( ! expired(userDTO.getForgotEmailcreated(), FORGOT_PASSWORD_EXPIRE), "Expired reset code");

    LOGGER.info("Updating lost password for UserId[" + userDTO.getId() + "]");

    userDTO.setForgotEmailCode(null);
    userDTO.setForgotEmailcreated(null);
    userDTO.setPasswordHash(bcrypt.hash(password));
    userRepository.save(userDTO);
  }

  protected boolean expired(Date date, Integer minutes) {
    return DateUtils.addMinutes(date, minutes).before(timeService.now());
  }

}
