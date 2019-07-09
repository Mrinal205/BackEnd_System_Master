package com.moonassist.persistence.order;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.type.Id;
import com.moonassist.type.OrderId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Builder
@Data
@ToString()
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class OrderDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<OrderId> id;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountDTO account;

  @Enumerated(EnumType.STRING)
  @Column(name = "exchange_name", nullable = false, updatable = false)
  private Exchange exchange;

  @Column(name = "symbol_pair")
  private String symbolPair;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, updatable = false)
  private OrderType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "offer", nullable = false, updatable = false)
  private OfferType offer;

  @Column(name = "price")
  private BigDecimal price;

  @Column(name = "amount")
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @Column(name = "exchange_order_id")
  private String exchangeOrderId;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(name = "updated")
  private Date updated;

}