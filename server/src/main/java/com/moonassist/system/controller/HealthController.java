package com.moonassist.system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

  @ResponseBody
  @RequestMapping(method = RequestMethod.GET)
  public String health() {

    return "OK";
  }

}
