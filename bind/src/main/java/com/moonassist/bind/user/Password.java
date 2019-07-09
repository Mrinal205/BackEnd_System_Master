package com.moonassist.bind.user;

import lombok.Builder;
import lombok.Data;

//TODO should we require 2fa code to change password if enabled?
@Data
@Builder
public class Password {

  String oldPassword;
  String newPassword;

}
