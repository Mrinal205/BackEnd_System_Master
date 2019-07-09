package com.moonassist.service.exchange;

import com.moonassist.bind.account.ExchangeEnum;

import lombok.Builder;

public class ExchangeConnectionParameters {

  public ExchangeConnectionParameters(ExchangeEnum exchangeEnum, String apiKey, String secret) {
    this.exchangeEnum = exchangeEnum;
    this.apiKey = apiKey;
    this.secret = secret;
  }

  private ExchangeEnum exchangeEnum;
  private String apiKey;
  private String secret;

  public ExchangeEnum exchange() {
    return exchangeEnum;
  }

  public String apiKey() {
    return apiKey;
  }

  public String secret() {
    return secret;
  }
}
