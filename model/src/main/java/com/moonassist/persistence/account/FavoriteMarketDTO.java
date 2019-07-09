package com.moonassist.persistence.account;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.moonassist.persistence.user.UserDTO;
import com.moonassist.type.FavoriteMarketId;
import com.moonassist.type.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorite_markets")
public class FavoriteMarketDTO {

    @javax.persistence.Id
    @Column(name = "id")
    private Id<FavoriteMarketId> id;

    @ManyToOne
    @JoinColumn(name = "exchange_id", nullable = false, updatable = false)
    private ExchangeDTO exchange;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private UserDTO user;

    @Column(name = "symbol_pair", nullable = false)
    private String symbolPair;

}
