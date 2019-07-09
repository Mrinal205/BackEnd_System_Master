package com.moonassist.service.exchange.balance;

import com.moonassist.bind.balance.BalanceValues;
import com.moonassist.service.bean.RequestDetail;
import lombok.SneakyThrows;
import org.knowm.xchange.coinbasepro.dto.CoinbaseProException;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExchangeBalanceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeBalanceService.class);

  private BalanceValues NO_RESULT = BalanceValues.builder().build();


  @SneakyThrows
  public Map<String, BalanceValues> fetchBalances(final RequestDetail requestDetail, final Optional<List<String>> symbols) {
    try {

      AccountInfo accountInfo = requestDetail.forRequest().getAccountService().getAccountInfo();

      if (accountInfo.getWallets().size() > 1) {
        LOGGER.warn("Wallet size is greater than 1, consider more wallets");
      }

      return (symbols != null && symbols.isPresent()) ?
        transformOnly(accountInfo.getWallet().getBalances(), symbols.get()) :
        transformAll(accountInfo.getWallet().getBalances());

    } catch (CoinbaseProException e) {
      throw new IllegalArgumentException(e.getMessage());
    }

  }

  private Map<String, BalanceValues> transformAll(Map<Currency, Balance> balances) {
    return balances.entrySet().stream()
        .collect(Collectors.toMap(
            entry -> entry.getKey().toString(),
            entry -> BalanceValues.builder()
                .available(entry.getValue().getAvailable())
                .total(entry.getValue().getTotal())
                .reserved(entry.getValue().getFrozen())
                .build()
            )
        );
  }

  private Map<String, BalanceValues> transformOnly(Map<Currency, Balance> balances, List<String> symbols) {
    return symbols.stream()
        .collect(Collectors.toMap(Function.identity(), s -> getSafeAvailable(balances, Currency.getInstance(s))));
  }

  private BalanceValues getSafeAvailable(Map<Currency, Balance> balances, Currency currency) {

    if ( ! balances.containsKey(currency)) {
      LOGGER.warn("Wallet does not contain currency[" + currency + "]");
      return NO_RESULT;
    }

    Balance balance  = balances.get(currency);

    return BalanceValues.builder()
        .available(balance.getAvailable())
        .total(balance.getTotal())
        .reserved(balance.getFrozen())
        .build();
  }

}
