package com.moonassist.service.exchange.connection;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.service.exchange.ExchangeConnectionParameters;

import lombok.Builder;

public class GDAXConnectionParameters extends ExchangeConnectionParameters {

  @Builder
  public GDAXConnectionParameters(ExchangeEnum exchange, String apiKey, String secret, String passphrase){
    super(exchange, apiKey, secret);
    this.passphrase = passphrase;
  }

  private String passphrase;

  public String passphrase() {
    return passphrase;
  }
}
