package com.moonassist.bind.balance;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BalanceValues {

  private BigDecimal available;

  private BigDecimal total;

  private BigDecimal reserved;

  private BigDecimal usdValue;

}
