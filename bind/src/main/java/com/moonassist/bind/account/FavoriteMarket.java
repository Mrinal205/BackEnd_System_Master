package com.moonassist.bind.account;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteMarket {

    private String favoriteMarketId;
    private String exchangeId;
    private String symbolPair;

    public void validate() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(exchangeId), "Exchange Id can not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(symbolPair), "ymbolPair can not be empty");
    }

}
