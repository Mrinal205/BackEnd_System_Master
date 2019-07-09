package com.moonassist.persistence.user;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<UserId> id;

  @Column(name = "name")
  private String name;

  @Column(name = "email")
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "password_hash_date")
  private Date passwordhashDate;

  @Column(name = "password_hash_version")
  private String passwordHashVersion;

  @OneToOne(mappedBy = "user")
  private AccountDTO account;

  @OneToOne(mappedBy = "user")
  private TwoFactorDTO twoFactorDTO;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private UserStatus status;

  @Column(name = "validate_code")
  private String validateCode;

  @Column(name = "forgot_password_code")
  private String forgotEmailCode;

  @Column(name = "forgot_password_created")
  private Date forgotEmailcreated;

}
