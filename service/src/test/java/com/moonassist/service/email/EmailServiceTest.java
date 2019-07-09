package com.moonassist.service.email;

import java.io.IOException;

import com.moonassist.service.DomainService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.moonassist.service.email.templates.TemplateFactory;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TemplateFactory.class, DomainService.class, EmailService.class})
public class EmailServiceTest {

  @Autowired
  private EmailService emailService;

  @Autowired
  private TemplateFactory templateFactory;

  private static final String FROM = "Testing Service<info@moonassist.com>";


  @Ignore
  @Test
  public void testDispatchEmail_integrationTest() throws UnirestException, IOException {

    Id<UserId> userId = new Id<>();

    String body = templateFactory.verifyEmail("Eric Anderson", userId, "SomeRandomCode");

    emailService.dispatchEmail("Moon Assist - Verify Account", "Eric Anderson<AndersonAnderson+test@gmail.com>", FROM, body);
  }

}
