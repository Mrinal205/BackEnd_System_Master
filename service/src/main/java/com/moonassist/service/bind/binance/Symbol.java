package com.moonassist.service.bind.binance;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 *
 * {
 *       "symbol":"ETHBTC",
 *       "status":"TRADING",
 *       "baseAsset":"ETH",
 *       "baseAssetPrecision":8,
 *       "quoteAsset":"BTC",
 *       "quotePrecision":8,
 *       "orderTypes":[
 *         "LIMIT",
 *         "LIMIT_MAKER",
 *         "MARKET",
 *         "STOP_LOSS_LIMIT",
 *         "TAKE_PROFIT_LIMIT"
 *       ],
 *       "icebergAllowed":false,
 *       "filters":[
 *         {
 *           "filterType":"PRICE_FILTER",
 *           "minPrice":"0.00000100",
 *           "maxPrice":"100000.00000000",
 *           "tickSize":"0.00000100"
 *         },
 *         {
 *           "filterType":"LOT_SIZE",
 *           "minQty":"0.00100000",
 *           "maxQty":"100000.00000000",
 *           "stepSize":"0.00100000"
 *         },
 *         {
 *           "filterType":"MIN_NOTIONAL",
 *           "minNotional":"0.00100000"
 *         }
 *       ]
 *     }
 *
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Symbol {

  @Builder
  public Symbol(String symbol, String status, List<Filter> filters) {
    this.status = status;
    this.symbol = symbol;
    this.filters = filters;
  }

  private String symbol;
  private String status;
  private List<Filter> filters;

  public Map<Filter.FilterType, Filter> filterMap() {

    return filters.stream().collect(Collectors.toMap(Filter::getFilterType, filter -> filter));

  }

}
