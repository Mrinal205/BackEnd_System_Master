package com.moonassist.bind.account;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum ExchangeEnum {

  BITTREX(true),
  BINANCE(true),
  GDAX(true),
  POLONIEX(false),
  KRAKEN(false),
  KUCOIN(false);

  ExchangeEnum(final Boolean active) {
    this.active = active;
  }

  private Boolean active;

  public static Set<ExchangeEnum> ACTIVE = Arrays.stream(ExchangeEnum.values())
      .filter( value -> value.active)
      .collect(Collectors.toSet());


}
