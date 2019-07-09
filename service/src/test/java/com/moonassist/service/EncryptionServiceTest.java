package com.moonassist.service;

import java.security.Key;
import java.util.Properties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EncryptionServiceTest.PropertyConfig.class, EncryptionService.class})
@ActiveProfiles("binance")
public class EncryptionServiceTest {

  @Autowired
  private EncryptionService encryptionService;

  private Key key = new SecretKeySpec("Bar12345Bar12345".getBytes(), "AES");

  @Test
  public void testEncrypt() throws Exception {

    String value = RandomStringUtils.randomAlphabetic(101);

    String encryptedString = encryptionService.encrypt(value, key);

    String result = encryptionService.decrypt(encryptedString, key);

    Assert.assertEquals(value, result);
  }

  @Test
  public void testEncrypt2() throws Exception {

    String value = RandomStringUtils.randomAlphabetic(101);
    String password = "asdfasdfasdf";

    SecretKey secretKey1 = EncryptionService.generate(password);
    SecretKey secretKey2 = EncryptionService.generate(password);

    String encryptedString = encryptionService.encrypt(value, secretKey1);

    String result = encryptionService.decrypt(encryptedString, secretKey2);

    Assert.assertEquals(value, result);
  }

  @Configuration
  public static class PropertyConfig {

    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
      PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
      final Properties properties = new Properties();
      properties.setProperty("encryption.key", "Bar12345Bar12345");
      ppc.setProperties(properties);

      return ppc;
    }
  }

}
