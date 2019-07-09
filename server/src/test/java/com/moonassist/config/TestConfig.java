package com.moonassist.config;

import com.moonassist.service.exchange.connection.ExchangeKeysValidatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.moonassist.service.email.EmailService;
import com.moonassist.util.MockEmailService;
import com.moonassist.util.MockExchangeKeysValidatorService;
import com.moonassist.util.TestUserHelper;


@Configuration
@ComponentScan(basePackages = {"com.moonassist.service", "com.moonassist.system.exception"})
public class TestConfig {

  @Bean
  public EmailService emailService() {
    return new MockEmailService();
  }

  @Bean
  public ExchangeKeysValidatorService exchangeKeysValidatorService() {
    return new MockExchangeKeysValidatorService();
  }

  @Bean
  public TestUserHelper testUserHelper() {
    return new TestUserHelper();
  }

}
