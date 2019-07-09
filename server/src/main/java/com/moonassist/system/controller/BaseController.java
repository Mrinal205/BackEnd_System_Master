package com.moonassist.system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;

public abstract class BaseController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountService accountService;

  public HttpHeaders headers() {

    HttpHeaders responseHeaders = new HttpHeaders();

    return responseHeaders;
  }

}
