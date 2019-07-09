package com.moonassist.bind.authenticate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyEmail {

  private String userId;

  private String code;

}
