package com.moonassist.persistence.order;

public enum OrderStatus {

  /**
   * Order has been created, but not yet sent to Exchange
   */
  CREATED,

  /**
   * Order is Created and Sent to Exchange for processing
   */
  SUBMITTED,

  /**
   * Sending the order to the exchange or Processing the order failed
   */
  FAILED,

  /**
   * Order has been fullfilled by the exchange.
   */
  FULLFILLED,

  /**
   * Order has been cancled
   */
  CANCELED,


}
