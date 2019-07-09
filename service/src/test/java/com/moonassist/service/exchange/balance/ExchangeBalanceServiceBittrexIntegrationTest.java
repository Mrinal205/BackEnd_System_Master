package com.moonassist.service.exchange.balance;

import com.google.common.collect.ImmutableList;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.BalanceValues;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.BaseTest;
import com.moonassist.service.bean.RequestDetail;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExchangeBalanceService.class})
public class ExchangeBalanceServiceBittrexIntegrationTest extends BaseTest {

  @Autowired
  private ExchangeBalanceService exchangeBalanceService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @Ignore //Remove once we have ETH in account
  @Test
  public void testGetBalance_Bittrex()  {

    RequestDetail requestDetail = RequestDetail.builder()
        .exchangeEnum(ExchangeEnum.BINANCE)
        .exchange(build(TEST_EXCHANGE.BINANCE.buildConnection()))
        .build();

    Map<String, BalanceValues> balances = exchangeBalanceService.fetchBalances(requestDetail, Optional.of(ImmutableList.of("ETH")));

    Assert.assertTrue("Failed Balance test, ETH[" + balances.get("ETH") + "]",
        balances.get("ETH").getAvailable().compareTo(BigDecimal.ZERO) > 0);
  }

}
