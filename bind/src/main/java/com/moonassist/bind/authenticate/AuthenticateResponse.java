package com.moonassist.bind.authenticate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticateResponse {

  String userId;
  String accountId;
  String email;
  String token;

}
