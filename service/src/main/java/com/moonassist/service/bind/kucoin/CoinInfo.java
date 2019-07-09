package com.moonassist.service.bind.kucoin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoinInfo {
  
  private String withdrawMinFee;
  private String coinType;
  private BigDecimal  withdrawMinAmount;
  private String  withdrawRemark;
  private String txUrl;
  private BigDecimal withdrawFeeRate;
  private Integer  confirmationCount;
  private String infoUrl;
  private Boolean enable;
  private String name;
  private Integer tradePrecision;
  private String depositRemark;
  private Boolean enableWithdraw;
  private Boolean enableDeposit;
  private String coin;
  
}
