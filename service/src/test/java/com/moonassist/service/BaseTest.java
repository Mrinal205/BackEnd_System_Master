package com.moonassist.service;

import com.google.common.collect.ImmutableMap;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.service.exchange.ExchangeConnectionFactory;
import com.moonassist.service.exchange.ExchangeConnectionParameters;
import com.moonassist.service.exchange.connection.GDAXConnectionParameters;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kucoin.KucoinExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;

import java.util.Collections;
import java.util.Map;

public class BaseTest {

  public Exchange build(ExchangeConnectionParameters exchangeConnectionParameters) {

    ExchangeSpecification exchangeSpecification;

    switch (exchangeConnectionParameters.exchange()) {

      case BITTREX:
        exchangeSpecification = new BittrexExchange().getDefaultExchangeSpecification();
        break;

      case KUCOIN:
        exchangeSpecification =  new KucoinExchange().getDefaultExchangeSpecification();
        break;

      case BINANCE:
        exchangeSpecification =  new BinanceExchange().getDefaultExchangeSpecification();
        break;

      case KRAKEN:
        exchangeSpecification =  new KrakenExchange().getDefaultExchangeSpecification();
        break;

      case GDAX:
        exchangeSpecification = new CoinbaseProExchange().getDefaultExchangeSpecification();
        GDAXConnectionParameters gdaxConnection = (GDAXConnectionParameters) exchangeConnectionParameters;
        exchangeSpecification.setExchangeSpecificParametersItem("passphrase", gdaxConnection.passphrase());
        break;

      case POLONIEX:
        exchangeSpecification = new PoloniexExchange().getDefaultExchangeSpecification();
        break;

      default:
        throw new RuntimeException("Unknown exchange: " + exchangeConnectionParameters.exchange());
    }

    exchangeSpecification.setApiKey(exchangeConnectionParameters.apiKey());
    exchangeSpecification.setSecretKey(exchangeConnectionParameters.secret());

    return ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
  }

  public enum TEST_EXCHANGE {
    BINANCE("f0yTuhwMubNDBzNA24aRE2eRezXjnTh9GKz2zMqObZo9HxNWCajoQwJrVM09XHOZ", "qqifoWzDLg68TwvwjrX7qybSZwEnWrfpRXzOdF8M8T3x7z3svufQonA3pL879ESn", Collections.EMPTY_MAP),
    BITTREX("d60b8a9d13664541ac62c957a7c355ea", "99feda1984fb48a8a0ec7d9706d72763", Collections.EMPTY_MAP),
    GDAX("f862ba40922a34aa8b8089ba658b93f6", "zp1d92jyj2OuXpqu3I0CIkcLmn4vLJYupOWm/NQV/TZH1BXm8cJZ0AmTKn9iEFy+Rp9OhqiBkMdLMF9BztxTeQ==", ImmutableMap.of("passphrase", "wywftsucam")),
    KRAKEN("6H+u8EIlKWqHwF+/MNwFoycAlm0+J4NJ3rhwl5vEp7G+w+eZC1wI1Xoy", "LlfHjFVJHOypmwQRyNe7k8F0gPEwaHAmhFstls9TJspA4j2eTz2ZUaVSpDYmeOIWi6veUG3XC9Z7Dzdl8mErmA==", Collections.EMPTY_MAP),
    KUCOIN("5af6a53fe0abb8220428a8b2", "e0a4b557-4cb6-4ae3-98b2-38b61d1a9558", Collections.EMPTY_MAP),
    POLONIEX("YNVKCD1S-7F0OZQUQ-JM7VW3IF-IKYSF6J3", "d38152a8f9fca790ff7c0c972e21cc6b56493b37aacae3dbea30980f56170f554f06ed387c696281eaf2581cecd9a102b39849d3c8228b198c3c5fba80bf43bb", Collections.EMPTY_MAP),
    ;

    TEST_EXCHANGE(String apiKey, String secret, Map<String, String> additional) {
      this.apiKey = apiKey;
      this.secret = secret;
      this.additional = additional;
    }

    private String apiKey;
    private String secret;
    private Map<String, String> additional;

    public String getApiKey() {
      return apiKey;
    }

    public String getSecret() {
      return secret;
    }


    public ExchangeConnectionParameters buildConnection() {
      return ExchangeConnectionFactory.build(ExchangeEnum.valueOf(this.name()), apiKey, secret, additional);
    }

  }

}
