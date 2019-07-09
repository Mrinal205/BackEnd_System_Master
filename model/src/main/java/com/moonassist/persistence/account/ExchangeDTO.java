package com.moonassist.persistence.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import com.moonassist.type.ExchangeId;
import com.moonassist.type.Id;
import com.vladmihalcea.hibernate.type.json.JsonStringType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "exchanges")
@TypeDefs({
    @TypeDef(name = "json", typeClass = JsonStringType.class),
})
@ToString(exclude = {"user", "address", "exchanges"})
public class ExchangeDTO {

  @javax.persistence.Id
  @Column(name = "id")
  private Id<ExchangeId> id;

  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false, updatable = false)
  private AccountDTO account;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(nullable = false)
  private Date updated;

  @Enumerated(EnumType.STRING)
  @Column(name = "exchange_name", nullable = false, updatable = false)
  private Exchange exchange;

  @Column(name = "api_key", nullable = false)
  private String apiKey;

  @Column(name = "secret", nullable = false)
  private String secret;

  @Type(type = "json")
  @Column(name = "additional", columnDefinition = "text")
  private Map<String, String> additional;
  
  @OneToMany(mappedBy = "exchange", fetch = FetchType.LAZY)
  private List<FavoriteMarketDTO> favoriteMarkets;

  @Enumerated(EnumType.STRING)
  @Column(name = "encryption_type")
  private EncryptionType encryptionType;

  @Column(name= "encryption_date")
  private Date encryptionDate;

  @Column(name= "encryption_alias")
  private String encryptionAlias;
}
