package com.moonassist.service.exchange.coinbasepro;

import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;

import com.google.common.base.Preconditions;

public class Convert {

  public static String convert(final CurrencyPair currencyPair) {

    return currencyPair.counter + "-" + currencyPair.base;
  }

  public static CurrencyPair convert(final String value) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(value));

    return new CurrencyPair(value.replace("-", "/"));
  }

}
