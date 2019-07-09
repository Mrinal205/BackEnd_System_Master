package com.moonassist.service.email;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.moonassist.service.email.templates.TemplateFactory;

@Service
public class ForgotPasswordEmailService {

  private static final String FROM = "Moon Assist <info@moonassist.com>";
  private static final String TITLE = "Moon Assist - Reset Password";

  private EmailService emailService;

  private TemplateFactory templateFactory;

  @Autowired
  public ForgotPasswordEmailService(EmailService emailService, TemplateFactory templateFactory) {
    this.emailService = emailService;
    this.templateFactory = templateFactory;
  }

  public void send(String email, String code) throws IOException, UnirestException {

    String body = templateFactory.forgotPassword(email, code);

    emailService.dispatchEmail(TITLE, email, FROM, body);
  }

}
