package com.moonassist.service.exchange.connection;

import com.moonassist.service.exchange.KeysValidator;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExchangeKeysValidatorService {

  private static final String BYPASS_KEY = "TEST:";

  public void checkKeys(com.moonassist.service.exchange.ExchangeConnectionParameters exchangeConnectionParameters) throws IOException {

    if (exchangeConnectionParameters.apiKey().contains(BYPASS_KEY)) {
      return;
    }

    switch (exchangeConnectionParameters.exchange()) {

      //GDAX requires passphrase
      case GDAX:
        new GDAXValidator().validate(exchangeConnectionParameters);
        break;

      case KUCOIN:
      case POLONIEX:
      case KRAKEN:
      case BINANCE:
      case BITTREX:
        new KeysValidator().validate(exchangeConnectionParameters);
        break;

      default:
        throw new IllegalStateException("Arrived at an invalid exchange: " + exchangeConnectionParameters.exchange());
    }
  }

}
