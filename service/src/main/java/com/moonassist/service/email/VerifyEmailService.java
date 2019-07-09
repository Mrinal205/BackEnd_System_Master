package com.moonassist.service.email;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.UserService;
import com.moonassist.service.email.templates.TemplateFactory;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class VerifyEmailService {

  @Autowired
  private EmailService emailService;

  @Autowired
  private UserService userService;

  private static final String FROM = "Moon Assist <info@moonassist.com>";
  private static final String TITLE = "Moon Assist - Account verification";

  @Autowired
  private TemplateFactory templateFactory;

  public void send(Id<UserId> userId) throws IOException, UnirestException {

    UserDTO userDTO = userService.find(userId);

    String body = templateFactory.verifyEmail(userDTO.getName(), userId, userDTO.getValidateCode());

    String email = userDTO.getName() + "<" + userDTO.getEmail() + ">";

    emailService.dispatchEmail(TITLE, email, FROM, body);
  }

}
