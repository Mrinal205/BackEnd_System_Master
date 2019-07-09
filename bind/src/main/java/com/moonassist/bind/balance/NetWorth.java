package com.moonassist.bind.balance;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetWorth {

  private BigDecimal value;

  private Date date;

  private String currency;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss.SSSZ")
  public Date getFormattedDate() {
    return date;
  }
}
