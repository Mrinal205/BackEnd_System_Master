package com.moonassist.system.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;

import com.moonassist.bind.balance.NetWorthResponse;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.connection.ExchangeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.WebUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.balance.Balance;
import com.moonassist.bind.balance.NetWorth;
import com.moonassist.bind.balance.NetWorthHistoricalResponse;
import com.moonassist.service.AuthenticationService;
import com.moonassist.service.BalanceService;
import com.moonassist.service.WalletService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.system.security.SecurityConstants;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

import lombok.SneakyThrows;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/balances")
public class BalanceController extends BaseController {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private BalanceService balanceService;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private WalletService walletService;

  @Autowired
  private ExchangeService exchangeService;

  @SneakyThrows
  @ResponseBody
  @RequestMapping(value = "/{exchange}/{symbols}", method = RequestMethod.GET)
  public Balance get(@PathVariable("exchange") String exchange, @PathVariable("symbols") String symbols, HttpServletRequest httpServletRequest) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(exchange), "exchange can not be empty");
    ExchangeEnum exchangeEnum = ExchangeEnum.valueOf(exchange);
    Preconditions.checkArgument(exchangeEnum != null, "Exchange " + exchange + " is not valid");
    Preconditions.checkArgument(StringUtils.isNotEmpty(symbols), "symbols can not be empty");

    List<String> symbolArray = Splitter.on(",").splitToList(symbols);

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(exchange))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(exchange)))
        .build();

    return balanceService.fetch(requestDetail, symbolArray);
  }

  @SneakyThrows
  @ResponseBody
  @RequestMapping(value = "/{exchange}", method = RequestMethod.GET)
  public Balance get(@PathVariable("exchange") String exchange, HttpServletRequest httpServletRequest) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(exchange), "exchange can not be empty");
    ExchangeEnum exchangeEnum = ExchangeEnum.valueOf(exchange);
    Preconditions.checkArgument(exchangeEnum != null, "Exchange " + exchange + " is not valid");

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(exchange))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(exchange)))
        .build();

    Balance balance = balanceService.fetch(requestDetail);
    return balance;
  }

  @SneakyThrows
  @ResponseBody
  @RequestMapping(value = "/networth", method = RequestMethod.GET)
  public NetWorthResponse getNetWorth(HttpServletRequest httpServletRequest) {

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    BigDecimal usd = walletService.getNetWorth(userId);

    BigDecimal btcUsdPrice = balanceService.valueOfBTC("USDT", ExchangeEnum.BINANCE);

    return NetWorthResponse.builder()
        .date(new Date())
        .usd(usd)
        .btc( usd.divide(btcUsdPrice, 8, RoundingMode.HALF_UP) )
        .build();
  }

  @SneakyThrows
  @ResponseBody
  @RequestMapping(value = "/networth/details", method = RequestMethod.GET)
  public Map<ExchangeEnum, Balance> getNetWorthDetails(HttpServletRequest httpServletRequest) {

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    Map<ExchangeEnum, Balance> result = walletService.getNetWorthDetailed(userId);


    return result;
  }

  @SneakyThrows
  @ResponseBody
  @RequestMapping(value = "/networth/historical", method = RequestMethod.GET)
  public NetWorthHistoricalResponse getNetWorthHistorial(@QueryParam("currency") String currency, HttpServletRequest httpServletRequest) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(currency), "Currency Type Required");
    Preconditions.checkArgument("USD".equals(currency), "Currency Types supported [USD]");

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    List<NetWorth> netWorthCollection = walletService.find(userId, WalletService.NetWorthTimePeriod.ALL);
    return NetWorthHistoricalResponse.builder()
        .usd(netWorthCollection)
        .build();
  }

}