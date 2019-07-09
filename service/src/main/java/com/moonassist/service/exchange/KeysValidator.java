package com.moonassist.service.exchange;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProException;
import org.knowm.xchange.service.account.AccountService;
import com.moonassist.service.exchange.ExchangeConnectionParameters;

import java.io.IOException;

public class KeysValidator {

  public ExchangeSpecificationFactory exchangeSpecificationFactory = new ExchangeSpecificationFactory();


  public void validate(ExchangeConnectionParameters exchangeConnectionParameters) throws IOException {

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
