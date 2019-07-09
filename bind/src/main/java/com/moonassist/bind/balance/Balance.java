package com.moonassist.bind.balance;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;

@Data
public class Balance {

  private Map<String, BalanceValues> valuesMap;

  @Builder
  public Balance(Map<String, BalanceValues> valuesMap) {
    this.valuesMap = valuesMap;
  }

  public void removeZeros() {

    valuesMap = valuesMap.entrySet().stream()
        .filter( entry -> BigDecimal.ZERO.compareTo(entry.getValue().getTotal()) == -1 )
        .collect( Collectors.toMap(x -> x.getKey(), x -> x.getValue()) );

  }

}
