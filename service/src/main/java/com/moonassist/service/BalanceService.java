package com.moonassist.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.moonassist.service.bean.RequestDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.Balance;
import com.moonassist.bind.balance.BalanceValues;
import com.moonassist.service.exchange.balance.ExchangeBalanceService;

@Service
public class BalanceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BalanceService.class);

  @Autowired
  private AccountService accountService;

  @Autowired
  private UserService userService;

  @Autowired
  private ExchangeBalanceService exchangeBalanceService;

  @Autowired
  private TickerService tickerService;

  MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_UP);


  //These are currencies that are in the Wallet Balances, but the exchanges don't have tickers for???
  private static final Map<ExchangeEnum, Set<String>> INVALID_CURRENCY =
      ImmutableMap.of(
          ExchangeEnum.BINANCE, ImmutableSet.of("CTR", "123", "456", "BTM", "ELC", "HCC", "LLT"),
          ExchangeEnum.GDAX, ImmutableSet.of("SNT", "BNB", "ADA", "TUSD", "NXS", "?", "$"),
          ExchangeEnum.BITTREX, ImmutableSet.of("BNB", "BTG")
      );


  public Balance fetch(final RequestDetail requestDetail, List<String> symbols) {

    Map<String, BalanceValues> valuesMap = exchangeBalanceService.fetchBalances(requestDetail, Optional.of(symbols));
    return Balance.builder()
        .valuesMap(valuesMap)
        .build();
  }

  public Balance fetch(final RequestDetail requestDetail) {

    Map<String, BalanceValues> valuesMap = exchangeBalanceService.fetchBalances(requestDetail, Optional.empty());
    return Balance.builder()
        .valuesMap(valuesMap)
        .build();
  }

  public BigDecimal valueOfBTC(final String currency, final ExchangeEnum exchange) {

    Preconditions.checkArgument("USD".equals( currency ) || "USDT".equals(currency), "only supports USD / USDT");

    Map<String, TickerService.Ticker> tickers = tickerService.fetchTickers(exchange);

    if (tickers.containsKey("BTC/USD")) {
      return tickers.get("BTC/USD").getPrice();
    }

    if (tickers.containsKey("BTC/USDT")) {
      return tickers.get("BTC/USDT").getPrice();
    }

    throw new RuntimeException("Expected to find a ticker value for BTC/USDT");
  }

  public BigDecimal valueOfUSD(final String currency, final ExchangeEnum exchange) {

    if ("USDT".equals(currency) || "USD".equals(currency)) {
      return BigDecimal.ONE;
    }

    if (INVALID_CURRENCY.get(exchange).contains(currency)) {
      return BigDecimal.ZERO;
    }

    if (ExchangeEnum.BINANCE.equals(exchange) && "BCC".equals(currency)) {
      return valueOfUSD("BCH", exchange);
    }

    if (ExchangeEnum.BINANCE.equals(exchange) && "YOYO".equals(currency)) {
      return valueOfUSD("YOYOW", exchange);
    }

    if (ExchangeEnum.BINANCE.equals(exchange) && "NANO".equals(currency)) {
      return valueOfUSD("XRB", exchange);
    }

    Map<String, TickerService.Ticker> tickers = tickerService.fetchTickers(exchange);

    if (tickers.containsKey(currency + "/USD")) {
      return tickers.get(currency + "/USD").getPrice();
    }

    if (tickers.containsKey(currency + "/USDT")) {
      return tickers.get(currency + "/USDT").getPrice();
    }

    if ("EUR".equals(currency)) {
      BigDecimal btcEur = tickers.get("BTC/EUR").getPrice();

      TickerService.Ticker ticker = (tickers.get("BTC/USDT") != null) ?
          tickers.get("BTC/USDT") :
          tickers.get("BTC/USD");

      return ticker.getPrice().divide(btcEur, MATH_CONTEXT);
    }

    if ("GBP".equals(currency)) {
      BigDecimal btcGbp = tickers.get("BTC/GBP").getPrice();

      TickerService.Ticker ticker = (tickers.get("BTC/USDT") != null) ?
          tickers.get("BTC/USDT") :
          tickers.get("BTC/USD");

      return ticker.getPrice().divide(btcGbp, MATH_CONTEXT);
    }

    if (tickers.containsKey(currency + "/BTC")) {
      BigDecimal temp = tickers.get(currency + "/BTC").getPrice();
      return (temp.multiply(tickers.get("BTC/USDT").getPrice()));
    }

    if (tickers.containsKey(currency + "/ETH")) {
      BigDecimal temp = tickers.get(currency + "/ETH").getPrice();
      return (temp.multiply(tickers.get("ETH/USDT").getPrice()));
    }

    LOGGER.warn("Could not find converter path for " + currency + "/USD(T) and exchange " + exchange);
    return BigDecimal.ZERO;
  }


  public BigDecimal completeValue(final Balance balance, final ExchangeEnum exchange) {

    BigDecimal result = balance.getValuesMap().entrySet().stream()
        .map( entry -> {

          if (BigDecimal.ZERO.compareTo(entry.getValue().getTotal()) == 0) {
            return BigDecimal.ZERO;
          }

          BigDecimal convertPrice = valueOfUSD(entry.getKey(), exchange);
          return convertPrice.multiply( entry.getValue().getTotal() );

        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return result.setScale(2, RoundingMode.HALF_UP);
  }

  public Balance completeValueBalance(final Balance balance, final ExchangeEnum exchange) {

    balance.getValuesMap().entrySet().stream()
        .forEach( entry -> {

          if (BigDecimal.ZERO.compareTo(entry.getValue().getTotal()) == 0) {
            entry.getValue().setUsdValue(BigDecimal.ZERO);
          }

          else {
            BigDecimal convertPrice = valueOfUSD(entry.getKey(), exchange);
            entry.getValue().setUsdValue(convertPrice.multiply(entry.getValue().getTotal()));
          }

        });

    return balance;
  }

}
