package com.moonassist.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RootController {

  @RequestMapping("/")
  @ResponseBody
  String get() {
    return "Moon Assist!";
  }

}
