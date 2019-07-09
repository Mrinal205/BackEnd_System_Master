package com.moonassist.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;
import com.moonassist.service.bean.RequestDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.Balance;
import com.moonassist.bind.balance.NetWorth;
import com.moonassist.persistence.account.NetWorthDTO;
import com.moonassist.persistence.account.NetWorthRepository;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.exchange.connection.ExchangeService;
import com.moonassist.service.exchange.ExchangeOrderServiceFactory;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@Service
public class WalletService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountService accountService;

  @Autowired
  private ExchangeOrderServiceFactory exchangeOrderServiceFactory;

  @Autowired
  private BalanceService balanceService;

  @Autowired
  private ExchangeService exchangeService;

  @Autowired
  private NetWorthRepository accountBalanceRepository;

  public BigDecimal getNetWorth(final Id<UserId> userId) {

    Preconditions.checkState(userId != null, "UserId for request is empty");

    UserDTO userDTO = userRepository.findOne(userId);
    Id<AccountId> accountId = userDTO.getAccount().getId();

    Set<ExchangeEnum> activeExchanges = ExchangeEnum.ACTIVE.stream()
        .filter(exchangeEnum -> accountService.hasExchange(accountId, exchangeEnum))
        .collect(Collectors.toSet());

    return activeExchanges.stream().map( exchangeEnum -> {

        RequestDetail requestDetail = RequestDetail.builder()
          .exchangeEnum(exchangeEnum)
          .userId(userId)
          .exchange(exchangeService.buildExchange(userId, exchangeEnum))
          .build();

        Balance balance = balanceService.fetch(requestDetail);

        return balanceService.completeValue(balance, exchangeEnum);

      })
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public Map<ExchangeEnum, Balance> getNetWorthDetailed(final Id<UserId> userId) {

    Preconditions.checkState(userId != null, "UserId for request is empty");

    UserDTO userDTO = userRepository.findOne(userId);
    Id<AccountId> accountId = userDTO.getAccount().getId();

    Set<ExchangeEnum> activeExchanges = ExchangeEnum.ACTIVE.stream()
        .filter(exchangeEnum -> accountService.hasExchange(accountId, exchangeEnum))
        .collect(Collectors.toSet());

    return activeExchanges.stream().map( exchangeEnum -> {

      RequestDetail requestDetail = RequestDetail.builder()
          .exchangeEnum(exchangeEnum)
          .userId(userId)
          .exchange(exchangeService.buildExchange(userId, exchangeEnum))
          .build();


      Balance balance = balanceService.fetch(requestDetail);
      balance.removeZeros();

      balance = balanceService.completeValueBalance(balance, exchangeEnum);

      return Maps.immutableEntry(exchangeEnum, balance);
    }).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
  }


  public List<NetWorth> find(final Id<UserId> userId, final NetWorthTimePeriod netWorthTimePeriod) {

    UserDTO userDTO = userRepository.findOne(userId);

    //480 includes 5 minutes data points for 24 hours
    PageRequest pageRequest = new PageRequest(0, 480, new Sort(Sort.Direction.DESC, "created"));

    List<NetWorthDTO> accountBalanceDTOS = accountBalanceRepository.findAllByAccountId(userDTO.getAccount().getId(), pageRequest);

    List<NetWorth> values = accountBalanceDTOS.stream()
        .map( accountBalanceDTO -> NetWorth.builder()
            .value(accountBalanceDTO.getValue())
            .date(accountBalanceDTO.getCreated())
            .build())
        .collect(Collectors.toList());

    BigDecimal bigDecimal = getNetWorth(userId);
    values.add(0, NetWorth.builder()
        .value(bigDecimal)
        .currency("USD")
        .date(new Date())
        .build());

    return values;
  }


  public static enum NetWorthTimePeriod {

    DAY,
    WEEK,
    MONTH,
    THREE_MONTH,
    ONE_YEAR,
    YEAR_TO_DATE,
    ALL

  }


}
