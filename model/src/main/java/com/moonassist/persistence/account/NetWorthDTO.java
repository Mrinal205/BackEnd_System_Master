package com.moonassist.persistence.account;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.moonassist.type.AccountBalanceId;
import com.moonassist.type.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Builder
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "net_worth")
public class NetWorthDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<AccountBalanceId> id;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountDTO account;

  @Column
  private BigDecimal value;

  @Column
  private String currency;

  @Column(nullable = false, updatable = false)
  private Date created;

}
