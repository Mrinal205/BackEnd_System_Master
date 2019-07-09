package com.moonassist.service.exchange;

public class ExchangeException extends RuntimeException {

  public ExchangeException(org.knowm.xchange.exceptions.ExchangeException e) {
    super(e);
  }

  public ExchangeException(String message) {
    super(message);
  }

}
