package com.moonassist.service.bean;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import lombok.Builder;
import lombok.Getter;
import org.knowm.xchange.Exchange;

import java.util.Optional;
import java.util.UUID;

@Getter
public class RequestDetail {

  private UUID requestId;

  private Id<UserId> userId;

  private Optional<ExchangeEnum> exchangeEnum ;

  private Exchange exchange;

  @Builder
  public RequestDetail(final Id<UserId> userId, final ExchangeEnum exchangeEnum, final Exchange exchange) {
    this.userId = userId;
    this.exchangeEnum = Optional.of(exchangeEnum);
    this.exchange = exchange;
    requestId = UUID.randomUUID();
  }

  public Exchange forRequest() {
    return getExchange();
  }

}
