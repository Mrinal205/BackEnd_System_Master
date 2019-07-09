package com.moonassist.bind.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

  String id;
  String name;
  String email;
  String password;
  boolean newsletter;

  //TODO need to build this out
  String affiliateToken;

}