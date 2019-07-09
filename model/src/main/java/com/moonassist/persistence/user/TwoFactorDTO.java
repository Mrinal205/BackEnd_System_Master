package com.moonassist.persistence.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.moonassist.type.Id;
import com.moonassist.type.TwoFactorId;
import lombok.ToString;

@ToString(exclude = {"user"})
@EqualsAndHashCode(exclude = {"user"})
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "twofactor")
public class TwoFactorDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<TwoFactorId> id;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(nullable = false)
  private Date updated;

  @Column
  private String key;

  @Enumerated(EnumType.STRING)
  @Column(updatable = false)
  private TwoFactoryType type;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private UserDTO user;

  @Column(name = "confirmed", nullable = false)
  private Boolean confirmed;

}
