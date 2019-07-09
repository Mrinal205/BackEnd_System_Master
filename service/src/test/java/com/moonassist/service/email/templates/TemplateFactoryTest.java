package com.moonassist.service.email.templates;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.DomainService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@RunWith(SpringRunner.class)

@ContextConfiguration(classes = {TemplateFactory.class})
public class TemplateFactoryTest {

  @Autowired
  private TemplateFactory templateFactory;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private DomainService domainService;

  @Test
  public void testVerifyEmail() throws IOException {
    Mockito.when(domainService.getAppDomain()).thenReturn("app.moonassist.com");

    String name = "Eric";
    Id<UserId> userId = new Id<>();
    String code = "SomeRandomCode";

    String body = templateFactory.verifyEmail(name, userId, code);

    Assert.assertTrue(body.contains(name));
    Assert.assertTrue(body.contains(userId.toString()));
    Assert.assertTrue(body.contains(code));
  }

}
