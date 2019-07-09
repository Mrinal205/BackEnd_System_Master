package com.moonassist.bind.account;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Preconditions;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class Exchange {

  public String id;

  public ExchangeEnum exchangeName;

  public String apiKey;

  public String secret;

  public Map<String, String> additional;

  public void validate() {
    Preconditions.checkArgument(exchangeName != null, "Exchange Name can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(apiKey), "api key can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(secret), "secret can not be empty");

    additional = (additional == null) ?
        Collections.EMPTY_MAP :
        additional;
  }

  public void mask() {
    apiKey = "**********" + StringUtils.substring(apiKey, apiKey.length() - 4, apiKey.length());
    secret = "**********" + StringUtils.substring(secret, secret.length() - 4, secret.length());
  }

}
