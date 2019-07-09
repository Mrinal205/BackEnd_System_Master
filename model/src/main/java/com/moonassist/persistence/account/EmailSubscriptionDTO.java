package com.moonassist.persistence.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

import com.moonassist.type.EmailSubscriptionId;
import com.moonassist.type.Id;
import lombok.ToString;

@ToString(exclude = {"account"})
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "email_subscription")
public class EmailSubscriptionDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<EmailSubscriptionId> id;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountDTO account;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Enumerated(EnumType.STRING)
  @Column(name = "type")
  private EmailSubscriptionType type;

  @Column(nullable = false)
  private Date updated;

}
