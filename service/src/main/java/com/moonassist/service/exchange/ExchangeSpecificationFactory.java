package com.moonassist.service.exchange;

import com.moonassist.service.exchange.connection.GDAXConnectionParameters;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.kucoin.KucoinExchange;
import org.knowm.xchange.poloniex.PoloniexExchange;
import org.springframework.stereotype.Service;

@Service
public class ExchangeSpecificationFactory {

  public ExchangeSpecification build(final com.moonassist.service.exchange.ExchangeConnectionParameters exchangeConnectionParameters){

    ExchangeSpecification result;

    switch (exchangeConnectionParameters.exchange()) {

      case BITTREX:
        result = new BittrexExchange().getDefaultExchangeSpecification();
        break;

      case KUCOIN:
        result =  new KucoinExchange().getDefaultExchangeSpecification();
        break;

      case BINANCE:
        result =  new BinanceExchange().getDefaultExchangeSpecification();
        break;

      case KRAKEN:
        result =  new KrakenExchange().getDefaultExchangeSpecification();
        break;

      case GDAX:
        result = new CoinbaseProExchange().getDefaultExchangeSpecification();
        GDAXConnectionParameters gdaxConnection = (GDAXConnectionParameters) exchangeConnectionParameters;
        result.setExchangeSpecificParametersItem("passphrase", gdaxConnection.passphrase());
        break;

      case POLONIEX:
        result = new PoloniexExchange().getDefaultExchangeSpecification();
        break;

      default:
        throw new RuntimeException("Unknown exchange: " + exchangeConnectionParameters.exchange());

    }

    result.setApiKey(exchangeConnectionParameters.apiKey());
    result.setSecretKey(exchangeConnectionParameters.secret());

    return result;
  }

}
