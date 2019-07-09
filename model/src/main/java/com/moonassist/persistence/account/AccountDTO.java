package com.moonassist.persistence.account;

import com.moonassist.persistence.user.UserDTO;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;


@Builder
@Data
@ToString(exclude = {"user", "address", "exchanges"})
@EqualsAndHashCode(exclude = {"user", "address", "exchanges"})
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts")
public class AccountDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<AccountId> id;

  @Column(name = "dob")
  private Date dob;

  @Column(name = "phone")
  private String phone;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(nullable = false, updatable = false)
  private Date updated;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private UserDTO user;

  @OneToOne(mappedBy = "account")
  private AddressDTO address;

  @OneToMany(mappedBy = "account")
  private List<ExchangeDTO> exchanges;

  @OneToMany(mappedBy = "account")
  private List<EmailSubscriptionDTO> emailSubscriptions;

}
