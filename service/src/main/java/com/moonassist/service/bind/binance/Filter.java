package com.moonassist.service.bind.binance;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * "filters":[
 *  *         {
 *  *           "filterType":"PRICE_FILTER",
 *  *           "minPrice":"0.00000100",
 *  *           "maxPrice":"100000.00000000",
 *  *           "tickSize":"0.00000100"
 *  *         },
 *  *         {
 *  *           "filterType":"LOT_SIZE",
 *  *           "minQty":"0.00100000",
 *  *           "maxQty":"100000.00000000",
 *  *           "stepSize":"0.00100000"
 *  *         },
 *  *         {
 *  *           "filterType":"MIN_NOTIONAL",
 *  *           "minNotional":"0.00100000"
 *  *         }
 *  *       ]
 *
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Filter {

  @Builder
  public Filter(FilterType filterType, BigDecimal minQty, BigDecimal maxQty, BigDecimal minNotional, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal tickSize, BigDecimal stepSize) {
    this.filterType = filterType;
    this.minQty = minQty;
    this.maxQty = maxQty;
    this.minNotional = minNotional;
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
    this.tickSize = tickSize;
    this.stepSize = stepSize;
  }

  private FilterType filterType;
  private BigDecimal minQty;
  private BigDecimal maxQty;
  private BigDecimal minNotional;
  private BigDecimal minPrice;
  private BigDecimal maxPrice;
  private BigDecimal tickSize;
  private BigDecimal stepSize;


  public enum FilterType {
    ICEBERG_PARTS,
    PRICE_FILTER,
    LOT_SIZE,
    MAX_NUM_ALGO_ORDERS,
    MIN_NOTIONAL,
  }


}
