package com.moonassist.service.exchange.connection;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.AccountService;
import com.moonassist.service.UserService;
import com.moonassist.service.exchange.ExchangeConnectionParameters;
import com.moonassist.service.exchange.ExchangeSpecificationFactory;
import com.moonassist.service.exchange.KeysValidator;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class ExchangeService {

  public static final String CACHE_KEY = "exchangeCache";

  private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

  @Autowired
  private UserService userService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private ExchangeSpecificationFactory exchangeSpecificationFactory;

  @Cacheable(value = CACHE_KEY)
  public Exchange buildExchange(final Id<UserId> userId, final ExchangeEnum exchangeEnum) {

    LOGGER.debug("Cache miss for exchange");

    UserDTO userDTO = userService.find(userId);
    Id<AccountId> accountId = userDTO.getAccount().getId();
    boolean hasExchange = accountService.hasExchange(accountId, exchangeEnum);
    Preconditions.checkArgument(hasExchange, "AccountId[" + accountId + "] does not have exchange " + exchangeEnum);

    Optional<AccountService.SecureExchangeInformation> exchangeDetails = accountService.findExchangeKeys(accountId, exchangeEnum);
    Preconditions.checkArgument(exchangeDetails.isPresent(), "AccountId [" + accountId + "] does not have keys for Exchange [" + exchangeEnum + "]");

    ExchangeConnectionParameters exchangeConnectionParameters = (exchangeEnum == ExchangeEnum.GDAX) ?
        new GDAXConnectionParameters(exchangeEnum, exchangeDetails.get().getApiKey(), exchangeDetails.get().getSecret(), exchangeDetails.get().getAdditional().get("passphrase")) :
        new ExchangeConnectionParameters(exchangeEnum, exchangeDetails.get().getApiKey(), exchangeDetails.get().getSecret());

    ExchangeSpecification exchangeSpecification = exchangeSpecificationFactory.build(exchangeConnectionParameters);

    Exchange exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);

    return exchange;
  }

}
