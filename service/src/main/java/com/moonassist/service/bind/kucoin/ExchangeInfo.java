package com.moonassist.service.bind.kucoin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeInfo {

  List<CoinInfo> data;

  public Map<String, CoinInfo> symbolsMap() {

    //TODO make this static (init once)
    return data.stream().collect(Collectors.toMap(CoinInfo::getCoin, item -> item));

  }
}
