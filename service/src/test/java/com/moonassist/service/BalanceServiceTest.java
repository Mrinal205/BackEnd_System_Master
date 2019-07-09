package com.moonassist.service;

import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.Balance;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.balance.ExchangeBalanceService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    TickerServiceTest.PropertyConfig.class,
    BalanceServiceTest.CachingConfiguration.class,
    ExchangeBalanceService.class,
    TickerService.class,
    BalanceService.class,
})
public class BalanceServiceTest extends BaseTest {

  @Autowired
  private BalanceService balanceService;

  @MockBean
  private AccountService accountService;

  @MockBean
  private UserService userService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private RequestDetail requestDetail;


  @Test
  public void testComplete_binance() {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(requestDetail.getUserId()).thenReturn(userId);
    when(requestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BINANCE));
    when(requestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.BINANCE.buildConnection()));

    Balance balance = balanceService.fetch(requestDetail);

    BigDecimal USDValue = balanceService.completeValue(balance, ExchangeEnum.BINANCE);

    Assert.assertNotNull(USDValue);
  }

  @Test
  public void testComplete_gdax() {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());

    when(requestDetail.getUserId()).thenReturn(userId);
    when(requestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.GDAX));
    when(requestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.GDAX.buildConnection()));

    Balance balance = balanceService.fetch(requestDetail);

    BigDecimal USDValue = balanceService.completeValue(balance, ExchangeEnum.GDAX);

    Assert.assertNotNull(USDValue);
  }

  @Test
  public void testComplete_bittrex() {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(requestDetail.getUserId()).thenReturn(userId);
    when(requestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BITTREX));
    when(requestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.BITTREX.buildConnection()));

    Balance balance = balanceService.fetch(requestDetail);

    BigDecimal USDValue = balanceService.completeValue(balance, ExchangeEnum.BITTREX);

    Assert.assertNotNull(USDValue);
  }

  @Test
  public void testValueOf_BTC() {

    BigDecimal result = balanceService.valueOfUSD("BTC", ExchangeEnum.BINANCE);

    Assert.assertNotNull(result);
  }

  @Test
  public void testValueOf_OMG() {

    BigDecimal result = balanceService.valueOfUSD("OMG", ExchangeEnum.BINANCE);

    Assert.assertNotNull(result);
    System.out.println(result);
  }

  @Test
  public void testValueOf_QuestionMark() {

    BigDecimal result = balanceService.valueOfUSD("?", ExchangeEnum.GDAX);

    Assert.assertNotNull(result);
    System.out.println(result);
  }

  @Configuration
  @EnableCaching
  static class CachingConfiguration {

    // Simulating your caching configuration
    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager(TickerService.CACHE_KEY);
    }

  }

}
