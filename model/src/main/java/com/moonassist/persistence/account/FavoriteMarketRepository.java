package com.moonassist.persistence.account;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moonassist.type.FavoriteMarketId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@Repository
public interface FavoriteMarketRepository extends JpaRepository<FavoriteMarketDTO, Id<FavoriteMarketId>> {

    /**
     * 
     * @param userId
     * @param pageable
     * @return
     */
    List<FavoriteMarketDTO> findAllByUserId(Id<UserId> userId, Pageable pageable);

}