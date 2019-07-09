package com.moonassist.persistence;

import java.util.Date;

import javax.transaction.Transactional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.AccountRepository;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.account.ExchangeDTO;
import com.moonassist.persistence.account.ExchangeRepository;
import com.moonassist.persistence.account.FavoriteMarketDTO;
import com.moonassist.persistence.account.FavoriteMarketRepository;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.type.AccountId;
import com.moonassist.type.ExchangeId;
import com.moonassist.type.FavoriteMarketId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { H2Config.class })
@Transactional
public class FavoriteMarketRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ExchangeRepository exchangeRepository;
    @Autowired
    private FavoriteMarketRepository favoriteMarketRepository;

    @Test
    public void testFindOne() {
        Date now = new Date(System.currentTimeMillis());

        Id<UserId> userId = new Id<>();
        Id<AccountId> accountId = new Id<>();
        Id<ExchangeId> exchangeId = new Id<>();
        Id<FavoriteMarketId> favoriteMarketId = new Id<>();

        UserDTO userDTO = userRepository.save(UserDTO.builder().id(userId).name("Test User").build());
        AccountDTO accountDTO = accountRepository.save(AccountDTO.builder().id(accountId).created(now).updated(now).user(userDTO).build());
        ExchangeDTO exchangeDTO = exchangeRepository.save(ExchangeDTO.builder().id(exchangeId).account(accountDTO).apiKey(RandomStringUtils.random(5))
                .secret(RandomStringUtils.random(5)).exchange(Exchange.BINANCE).created(now).updated(now).build());

        FavoriteMarketDTO favoriteMarketDTO = favoriteMarketRepository.save(FavoriteMarketDTO.builder().id(favoriteMarketId)
                .symbolPair(RandomStringUtils.random(5)).exchange(exchangeDTO).user(userDTO).build());

        FavoriteMarketDTO result = favoriteMarketRepository.findOne(favoriteMarketId);

        Assert.assertNotNull("Failed to find favorite market", result);
    }

}
