package com.moonassist.bind.authenticate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TwoFactor {

  String userId;
  String type;
  String number;

}
