package com.moonassist.bind.account;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSubscription {

  private String id;

  private EmailSubscriptionEnum type;

  public void validate() {
    Preconditions.checkArgument(type != null, "email subscriptin type is required");
  }

}
