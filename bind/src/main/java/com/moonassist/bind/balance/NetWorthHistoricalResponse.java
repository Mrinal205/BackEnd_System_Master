package com.moonassist.bind.balance;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetWorthHistoricalResponse {

  private List<NetWorth> usd;

}
