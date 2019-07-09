package com.moonassist.service.exchange;

import java.util.Map;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.service.AccountService;
import com.moonassist.service.exchange.connection.GDAXConnectionParameters;

public class ExchangeConnectionFactory {

  public static com.moonassist.service.exchange.ExchangeConnectionParameters build(ExchangeEnum exchange, String apiKey, String secret, Map<String, String> additionalParameters) {

    switch (exchange) {

      case GDAX:
        return GDAXConnectionParameters.builder()
            .exchange(exchange)
            .apiKey(apiKey)
            .secret(secret)
            .passphrase(additionalParameters.get("passphrase"))
            .build();

      default:
        return new com.moonassist.service.exchange.ExchangeConnectionParameters(exchange, apiKey, secret);

    }

  }

  public static com.moonassist.service.exchange.ExchangeConnectionParameters build(ExchangeEnum exchangeEnum, AccountService.SecureExchangeInformation secureExchangeInformation) {
    return build(exchangeEnum, secureExchangeInformation.getApiKey(), secureExchangeInformation.getSecret(), secureExchangeInformation.getAdditional());
  }

}
