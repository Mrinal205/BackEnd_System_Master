package com.moonassist.service.exchange.connection;

import com.moonassist.service.exchange.ExchangeConnectionParameters;
import com.moonassist.service.exchange.KeysValidator;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProException;
import org.knowm.xchange.service.account.AccountService;

import java.io.IOException;

public class GDAXValidator extends KeysValidator {

  @Override
  public void validate(ExchangeConnectionParameters exchangeConnectionParameters) throws IOException {
    GDAXConnectionParameters gdaxConnection = (GDAXConnectionParameters) exchangeConnectionParameters;

    ExchangeSpecification exchangeSpecification = exchangeSpecificationFactory.build(exchangeConnectionParameters);

    try {
      Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);

      AccountService accountService = exchange.getAccountService();
      accountService.getAccountInfo();
    } catch (CoinbaseProException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

}
