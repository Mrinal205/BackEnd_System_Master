package com.moonassist.util;

import javax.annotation.concurrent.NotThreadSafe;

import com.moonassist.service.UserService;
import com.moonassist.service.email.EmailService;

@NotThreadSafe
public class MockEmailService extends EmailService {

  private String email = null;

  private static final String START_WORD = "code=";


  @Override
  public void dispatchEmail(String subject, String to, String from, String body) {
    email = body;
  }

  //Example https://test-ui-moonassist.herokuapp.com/verify?uId=e6483bfd-5f34-4e07-a901-2bb2c981f2ee&code=Np0cnuYy2cJvfElOjQz10kH
  public String getLastCode() {

    int startIndex = email.indexOf(START_WORD);
    return email.substring(startIndex + START_WORD.length(), startIndex + START_WORD.length() + UserService.VERIFY_CODE_LENGTH);
  }

}
