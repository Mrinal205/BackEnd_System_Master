package com.moonassist.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.FavoriteMarket;
import com.moonassist.persistence.account.ExchangeDTO;
import com.moonassist.persistence.account.ExchangeRepository;
import com.moonassist.persistence.account.FavoriteMarketDTO;
import com.moonassist.persistence.account.FavoriteMarketRepository;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.type.FavoriteMarketId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@Service
public class FavoriteMarketsService {

    @Autowired
    private FavoriteMarketRepository favoriteMarketRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;

    /**
     * 
     * @param userId
     * @return
     */
    public List<FavoriteMarket> all(Id<UserId> userId) {
        Preconditions.checkArgument(userId != null, "userId can not be empty");
        List<FavoriteMarket> result = new ArrayList<>();
        Pageable topTwenty = new PageRequest(0, 20);

        List<FavoriteMarketDTO> favoriteMarkets = favoriteMarketRepository.findAllByUserId(userId, topTwenty);
        if (CollectionUtils.isNotEmpty(favoriteMarkets)) {
            favoriteMarkets.stream().forEach(fm -> {
                result.add(FavoriteMarket.builder().favoriteMarketId(fm.getId().toString()).symbolPair(fm.getSymbolPair())
                        .exchangeId(fm.getExchange().getId().toString()).build());
            });
        }
        return result;
    }

    /**
     * 
     * @param favoriteMarket
     */
    public FavoriteMarket save(FavoriteMarket favoriteMarket) {
        ExchangeDTO exchangeDTO = exchangeRepository.findOne(Id.from(favoriteMarket.getExchangeId()));
        UserDTO userDTO = exchangeDTO.getAccount().getUser();
        FavoriteMarketDTO favoriteMarketDTO = FavoriteMarketDTO.builder().id(new Id<FavoriteMarketId>()).symbolPair(favoriteMarket.getSymbolPair())
                .exchange(exchangeDTO).user(userDTO).build();
        favoriteMarket.setFavoriteMarketId(favoriteMarketDTO.getId().toString());
        favoriteMarketRepository.save(favoriteMarketDTO);
        return favoriteMarket;
    }

    /**
     * 
     * @param favoriteMarketId
     */
    public void delete(Id<FavoriteMarketId> favoriteMarketId) {
        Preconditions.checkArgument(favoriteMarketId != null, "favoriteMarketId can not be empty");
        favoriteMarketRepository.delete(favoriteMarketId);
    }

}