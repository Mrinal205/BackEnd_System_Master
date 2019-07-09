package com.moonassist.service.exchange.balance;

import com.google.common.collect.ImmutableList;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.BalanceValues;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.BaseTest;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExchangeBalanceService.class})
@ActiveProfiles("binance")
public class ExchangeBalanceServiceBinanceIntegrationTest extends BaseTest {

  @Autowired
  private ExchangeBalanceService exchangeBalanceService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private RequestDetail mockRequestDetail;

  @Ignore //Remove once we have ETH in account
  @Test
  public void testGetBalance_Binance()  {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(mockRequestDetail.getUserId()).thenReturn(userId);
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.BINANCE));
    when(mockRequestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.BINANCE.buildConnection()));


    Map<String, BalanceValues> balances = exchangeBalanceService.fetchBalances(mockRequestDetail, Optional.of(ImmutableList.of("ETH")));

    Assert.assertTrue("Failed Available Balance test, ETH[" + balances.get("ETH") + "]",
        balances.get("ETH").getAvailable().compareTo(BigDecimal.ZERO) > 0);

    Assert.assertTrue("Failed Total Balance test, ETH[" + balances.get("ETH") + "]",
        balances.get("ETH").getTotal().compareTo(BigDecimal.ZERO) > 0);

  }

}
