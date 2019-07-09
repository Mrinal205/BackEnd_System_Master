package com.moonassist.bind.order;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Preconditions;
import com.moonassist.bind.account.ExchangeEnum;

import lombok.Builder;
import lombok.Data;


@JsonInclude(NON_NULL)
@Data
public class Order {

  private String id;

  private BigDecimal amount;

  private ExchangeEnum exchangeName;

  private BigDecimal filled;

  private String exchangeOrderId;

  private OfferType offerType;

  private OrderType orderType;

  private BigDecimal price;

  private OrderStatus status;

  private String symbolPair;

  private Date timestamp;

  private BigDecimal total;

  @Builder(toBuilder = true)
  public Order(String id, String exchangeOrderId, String symbolPair, OfferType offerType, OrderType orderType, ExchangeEnum exchangeName, BigDecimal price, BigDecimal amount, OrderStatus status,
      Date timestamp, BigDecimal filled) {
    this.id = id;
    this.exchangeOrderId = exchangeOrderId;
    this.symbolPair = symbolPair;
    this.offerType = offerType;
    this.orderType = orderType;
    this.exchangeName = exchangeName;
    this.price = price;
    this.amount = amount;
    this.status = status;
    this.timestamp = timestamp;
    this.filled = filled;
    this.total = (amount != null && price != null) ?
      amount.multiply(price) :
      null;
  }

  public void validate() {
    //TODO do better validation of symbol pairs?
    Preconditions.checkArgument(StringUtils.isNoneEmpty(symbolPair), "SymbolPair can not be empty");

    Preconditions.checkArgument(orderType != null, "OrderType is required");
    Preconditions.checkArgument(exchangeName != null, "Exchange Name is required");
    Preconditions.checkArgument(amount != null, "amount is required");
    Preconditions.checkArgument(amount.compareTo(BigDecimal.ZERO) > 0, "amount must be greater than 0");

    if (orderType == OrderType.LIMIT) {
      Preconditions.checkArgument(price != null, "Price can not be empty");
      Preconditions.checkArgument(price.compareTo(BigDecimal.ZERO) > 0, "Price must be greater than Zero");
    }

    if (orderType == OrderType.MARKET) {
      Preconditions.checkArgument(price == null, "Price is not valid on market orders");
    }

  }

}