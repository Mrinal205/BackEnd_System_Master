package com.moonassist.mailerlite.bind;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group {

  private String id;
  private String name;
  private Integer total;
  private Integer active;
  private Integer unsubscribed;
  private Integer bounced;

}

