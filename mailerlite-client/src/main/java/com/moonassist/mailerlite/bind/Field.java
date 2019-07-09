package com.moonassist.mailerlite.bind;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Field {

  private String key;
  private String value;
  private String type;

}
