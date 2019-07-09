package com.moonassist.service;

import org.junit.Assert;
import org.junit.Test;

public class EmailValidationServiceTest {

  private EmailValidationService emailValidationService = new EmailValidationService();


  @Test
  public void testIsApproved() {

//    Assert.assertFalse(emailValidationService.isApproved("blahblah@gmail.com"));

    Assert.assertTrue(emailValidationService.isApproved("andersonanderson@gmail.com"));
    Assert.assertTrue(emailValidationService.isApproved("eric@moonassist.com"));

//    Assert.assertTrue(emailValidationService.isApproved("andersonanderson+test@gmail.com"));
  }

}
