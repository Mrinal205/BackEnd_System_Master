package com.moonassist.service.bind.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeInfo {

  private List<Symbol> symbols;

  public Map<String, Symbol> symbolsMap() {

    //TODO make this static (init once)
    return symbols.stream().collect(Collectors.toMap(Symbol::getSymbol, item -> item));

  }

}
