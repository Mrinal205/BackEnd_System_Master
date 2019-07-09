package com.moonassist.service.exchange;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.service.exchange.connection.ExchangeKeysValidatorService;
import com.moonassist.service.exchange.connection.GDAXConnectionParameters;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class ExchangeKeysValidatorServiceTest {

  private ExchangeKeysValidatorService exchangeKeysValidatorService = new ExchangeKeysValidatorService();

  @Test
  @Ignore// need to mock out the call to GDAX
  public void testCheckKeys_GDAX() throws IOException {

    GDAXConnectionParameters connection = GDAXConnectionParameters.builder()
        .exchange(ExchangeEnum.GDAX)
        .apiKey("blah")
        .secret("qwefasefasdfhljxeboumvtm3j4ULCjWdhkQmuRra8nBlQwfvPLqujystFHMgJrbTIxK4g==")
        .passphrase("not_my_passphrase")
        .build();


    exchangeKeysValidatorService.checkKeys(connection);
  }

}
