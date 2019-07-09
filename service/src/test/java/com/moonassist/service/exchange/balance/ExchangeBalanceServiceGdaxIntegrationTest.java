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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ExchangeBalanceService.class})
public class ExchangeBalanceServiceGdaxIntegrationTest extends BaseTest {

  @Autowired
  private ExchangeBalanceService exchangeBalanceService;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountService accountService;

  @MockBean
  private RequestDetail mockRequestDetail;

  @Test
  public void testGetBalance_Gdax()  {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());
    when(mockRequestDetail.getUserId()).thenReturn(userId);
    when(mockRequestDetail.getExchangeEnum()).thenReturn(Optional.of(ExchangeEnum.GDAX));
    when(mockRequestDetail.forRequest()).thenReturn(build(TEST_EXCHANGE.GDAX.buildConnection()));

    Map<String, BalanceValues> balances = exchangeBalanceService.fetchBalances(mockRequestDetail, Optional.of(ImmutableList.of("ETH")));

    Assert.assertTrue("Failed Balance test, ETH[" + balances.get("ETH") + "]",
        balances.get("ETH").getAvailable().compareTo(BigDecimal.ZERO) > 0);
  }

}