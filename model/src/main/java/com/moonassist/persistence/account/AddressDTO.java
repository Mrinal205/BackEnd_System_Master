package com.moonassist.persistence.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.moonassist.type.AddressId;
import com.moonassist.type.Id;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "address")
public class AddressDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<AddressId> id;

  @OneToOne
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountDTO account;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(nullable = false, updatable = false)
  private Date updated;

  @Column
  private String line1;

  @Column
  private String line2;

  @Column
  private String city;

  @Column
  private String province;

  @Column
  private String postal;

  @Column
  private String country;

}
