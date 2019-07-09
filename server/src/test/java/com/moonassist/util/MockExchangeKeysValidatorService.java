package com.moonassist.util;

import com.moonassist.service.exchange.ExchangeConnectionParameters;
import com.moonassist.service.exchange.connection.ExchangeKeysValidatorService;

import java.io.IOException;

//TODO setup in memory http server mock (WireMock)
public class MockExchangeKeysValidatorService extends ExchangeKeysValidatorService {

  @Override
  public void checkKeys(ExchangeConnectionParameters exchangeConnectionParameters) throws IOException {
    //do nothing
  }
}
