package com.moonassist.service.email.templates;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.collect.ImmutableMap;
import com.moonassist.service.DomainService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

@Service
public class TemplateFactory {

  private MustacheFactory mustacheFactory = new DefaultMustacheFactory();

  @Autowired
  private DomainService domainService;

  public String verifyEmail(String name, Id<UserId> userId, String code) throws IOException {

    Mustache mustache = mustacheFactory.compile("VerifyEmail.mustache");

    final Writer writer = new StringWriter();
    mustache.execute(writer, ImmutableMap.of("host", domainService.getAppDomain(), "name", name, "userId", userId, "code", code));
    writer.flush();

    return writer.toString();
  }

  public String forgotPassword(String email, String code) throws IOException {

    Mustache mustache = mustacheFactory.compile("ForgotPassword.mustache");

    final Writer writer = new StringWriter();
    mustache.execute(writer, ImmutableMap.of("host", domainService.getAppDomain(), "email", email, "code", code));
    writer.flush();

    return writer.toString();
  }

}
