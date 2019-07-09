package com.moonassist.service.exchange;

import com.google.common.base.Preconditions;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.exchange.binance.BinanceOrderService;
import com.moonassist.service.exchange.bittrex.BittrexOrderService;
import com.moonassist.service.exchange.coinbasepro.GDAXOrderService;
import com.moonassist.service.exchange.kucoin.KucoinOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ExchangeOrderServiceFactory {

  @Autowired
  @Qualifier("ExchangeOrderService")
  private ExchangeOrderService exchangeOrderService;

  @Autowired
  private BinanceOrderService binanceOrderService;

  @Autowired
  private BittrexOrderService bittrexOrderService;

  @Autowired
  private KucoinOrderService kucoinOrderService;

  @Autowired
  private GDAXOrderService gdaxOrderService;

  public ExchangeOrderService factory(final RequestDetail requestDetail) {

    Preconditions.checkState(requestDetail.getExchangeEnum().isPresent(), "Exchange param is not present");

    switch (requestDetail.getExchangeEnum().get()) {

      case BINANCE:
        return binanceOrderService;

      case KUCOIN:
        return kucoinOrderService;

      case BITTREX:
        return bittrexOrderService;

      case GDAX:
        return gdaxOrderService;

      default:
        return exchangeOrderService;
    }

  }

}
