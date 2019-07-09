package com.moonassist.service;

import com.blueconic.browscap.BrowsCapField;
import com.blueconic.browscap.Capabilities;
import com.blueconic.browscap.ParseException;
import com.blueconic.browscap.UserAgentParser;
import com.blueconic.browscap.UserAgentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Address;
import com.moonassist.bind.account.EmailSubscription;
import com.moonassist.bind.account.EmailSubscriptionEnum;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.account.LoginEvent;
import com.moonassist.bind.account.PersonalDetails;
import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.AccountRepository;
import com.moonassist.persistence.account.AddressDTO;
import com.moonassist.persistence.account.AddressRepository;
import com.moonassist.persistence.account.EmailSubscriptionDTO;
import com.moonassist.persistence.account.EmailSubscriptionRepository;
import com.moonassist.persistence.account.EmailSubscriptionType;
import com.moonassist.persistence.account.EncryptionType;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.account.ExchangeDTO;
import com.moonassist.persistence.account.ExchangeRepository;
import com.moonassist.persistence.user.EventType;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.authentication.TwoFactorAuthenticationService;
import com.moonassist.service.exchange.ExchangeConnectionFactory;
import com.moonassist.service.exchange.ExchangeConnectionParameters;
import com.moonassist.service.exchange.connection.ExchangeKeysValidatorService;
import com.moonassist.service.exchange.connection.ExchangeService;
import com.moonassist.type.AccountId;
import com.moonassist.type.EmailSubscriptionId;
import com.moonassist.type.ExchangeId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import com.neovisionaries.i18n.CountryCode;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//TODO refactor this class into smaller pieces
@Service
public class AccountService implements InitializingBean {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

  private AccountRepository accountRepository;

  private UserRepository userRepository;

  private AddressRepository addressRepository;

  private ExchangeRepository exchangeRepository;

  private EventsService eventsService;

  private TwoFactorAuthenticationService twoFactorAuthenticationService;

  private TimeService timeService;

  private EmailSubscriptionRepository emailSubscriptionRepository;

  private ExchangeKeysValidatorService exchangeKeysValidatorService;

  private UserAgentParser userAgentParser;

  private EncryptionService encryptionService;

  private Key aesKey;

  @Value("${encryption.exchange.alias1.key}")
  private String key;

  @Autowired
  public AccountService(AccountRepository accountRepository, UserRepository userRepository, AddressRepository addressRepository, ExchangeRepository exchangeRepository,
      EventsService eventsService, TwoFactorAuthenticationService twoFactorAuthenticationService, TimeService timeService, EmailSubscriptionRepository emailSubscriptionRepository,
      ExchangeKeysValidatorService exchangeKeysValidatorService, EncryptionService encryptionService) {
    this.accountRepository = accountRepository;
    this.userRepository = userRepository;
    this.addressRepository = addressRepository;
    this.exchangeRepository = exchangeRepository;
    this.eventsService = eventsService;
    this.twoFactorAuthenticationService = twoFactorAuthenticationService;
    this.timeService = timeService;
    this.emailSubscriptionRepository = emailSubscriptionRepository;
    this.exchangeKeysValidatorService = exchangeKeysValidatorService;
    this.encryptionService = encryptionService;


    //TODO move to bean
    try {
      userAgentParser = new UserAgentService().loadParser(Arrays.asList(BrowsCapField.BROWSER, BrowsCapField.BROWSER_TYPE,
          BrowsCapField.BROWSER_MAJOR_VERSION, BrowsCapField.DEVICE_TYPE, BrowsCapField.PLATFORM, BrowsCapField.PLATFORM_VERSION));
    } catch (IOException | ParseException e) {
      LOGGER.error("Unable to load userAgentParser");
    }
  }

  public void init() {
    Preconditions.checkState(StringUtils.isNotEmpty(key), "Found an empty key");
    aesKey = EncryptionService.generate(key);
  }

  public AccountDTO getOrCreate(Id<UserId> userId) {

    Preconditions.checkArgument(userId != null, "userId can not be empty");
    UserDTO userDTO = userRepository.findOne(userId);

    Preconditions.checkArgument(userDTO != null, "No user for userId: " + userId);

    if (userDTO.getAccount() == null) {

      Date now = new Date();

      AccountDTO accountDTO = AccountDTO.builder()
          .created(now)
          .updated(now)
          .id(new Id<>())
          .user(userDTO)
          .build();

      return accountRepository.save(accountDTO);
    }

    return userDTO.getAccount();
  }

  public Account find(Id<AccountId> accountId, Id<UserId> userId) {

    AccountDTO accountDTO = accountRepository.findOne(accountId);
    Preconditions.checkArgument(accountDTO != null, "accountId [" + accountId + "] is invalid");
    Preconditions.checkArgument(accountDTO.getUser().getId().equals(userId), "Invalid User Request");
    UserDTO userDTO = userRepository.findOne(userId);

    Account.AccountBuilder account = Account.builder()
        .id(accountDTO.getId().toString())
        .personalDetails(
            PersonalDetails.builder()
                .name(userDTO.getName())
                .phone(accountDTO.getPhone())
                .dateOfBirth(accountDTO.getDob())
                .build()
        )
        .twoFactorEnabled(twoFactorAuthenticationService.isActive(userDTO.getTwoFactorDTO()));

    if (accountDTO.getAddress() != null) {
      account.address(
          Address.builder()
              .line1(accountDTO.getAddress().getLine1())
              .line2(accountDTO.getAddress().getLine2())
              .city(accountDTO.getAddress().getCity())
              .province(accountDTO.getAddress().getProvince())
              .postal(accountDTO.getAddress().getPostal())
              .country(accountDTO.getAddress().getCountry())
              .build()
      );
    }

    //Find exchanges
    Set<com.moonassist.bind.account.Exchange> exchanges = accountDTO.getExchanges().stream().map(exchangeDTO -> {
      com.moonassist.bind.account.Exchange exchange = com.moonassist.bind.account.Exchange.builder()
          .id(exchangeDTO.getId().value())
//          .secret(exchangeDTO.getSecret())
//          .apiKey(exchangeDTO.getApiKey())
          .apiKey("**********")
          .secret("**********")
          .exchangeName(ExchangeEnum.valueOf(exchangeDTO.getExchange().name()))
          .additional(exchangeDTO.getAdditional())
          .build();
      exchange.mask();
      return exchange;
    }).collect(Collectors.toSet());
    account.exchanges(exchanges);

    //Find emailSubscriptions
    Set<EmailSubscription> emailSubscriptions = accountDTO.getEmailSubscriptions().stream().map(emailSubscriptionDTO -> {
      return EmailSubscription.builder()
          .id(emailSubscriptionDTO.getId().value())
          .type(EmailSubscriptionEnum.LOGIN.valueOf(emailSubscriptionDTO.getType().name()))
          .build();
    }).collect(Collectors.toSet());
    account.emailSubscriptions(emailSubscriptions);

    //Find last login
    List<LoginEvent> loginEvents = eventsService.all(userId, EventType.LOGIN).stream().map(eventDTO -> {
      //TODO add cache lookup
      final Capabilities capabilities = userAgentParser.parse(eventDTO.getUserAgent());
      return LoginEvent.builder()
          .date(eventDTO.getCreated())
          .ipAddress(eventDTO.getIpAddress())
          .userAgent(capabilities.getBrowser() + ", " + capabilities.getPlatform())
          .build();
    } ).collect(Collectors.toList());
    account.loginEvents(loginEvents);

    return account.build();
  }

  public void update(Id<AccountId> accountId, Account account) {

    AccountDTO accountDTO = accountRepository.findOne(accountId);
    Preconditions.checkArgument(accountDTO != null, "Invalid accountId : " + accountId);

    accountDTO.getUser().setName(account.personalDetails.getName());

    accountDTO
        .setDob(account.personalDetails.getDateOfBirth())
        .setPhone(account.personalDetails.getPhone());

    //TODO make addresses historical?

    if (accountDTO.getAddress() != null) {
      accountDTO.getAddress()
          .setLine1(account.address.line1)
          .setLine2(account.address.line2)
          .setCity(account.address.city)
          .setPostal(account.address.postal)
          .setProvince(account.address.province)
          .setCountry(account.address.country)
          .setUpdated(timeService.now());
    } else {
      AddressDTO addressDTO = AddressDTO.builder()
          .id(new Id())
          .account(accountDTO)
          .line1(account.address.line1)
          .line2(account.address.line2)
          .city(account.address.city)
          .province(account.address.province)
          .country(account.address.country)
          .created(timeService.now())
          .updated(timeService.now())
          .build();

      addressRepository.save(addressDTO);
      accountDTO.setAddress(addressDTO);
    }

    accountDTO.setUpdated(timeService.now());
    accountRepository.save(accountDTO);
  }

  public AccountDTO saveAddress(Id<AccountId> id, String line1, String line2, String city, String province, String postal, String country) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(line1), "line1 can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(line2), "line2 can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(city), "city can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(province), "province can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(postal), "postal can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(country), "country can not be empty");
    Preconditions.checkArgument(country.length() == 3, "country must be three characters");

    CountryCode countryCode = CountryCode.getByCode(country);
    Preconditions.checkArgument(countryCode != null, "country code : " + country + " is not valid");

    Date now = new Date();

    AccountDTO accountDTO = accountRepository.findOne(id);
    if (accountDTO.getAddress() != null) {
      accountDTO.getAddress()
          .setLine1(line1)
          .setLine2(line2)
          .setCity(city)
          .setPostal(postal)
          .setProvince(province)
          .setCountry(country)
          .setUpdated(now);
    } else {
      AddressDTO addressDTO = AddressDTO.builder()
          .id(new Id())
          .account(accountDTO)
          .line1(line1)
          .line2(line2)
          .city(city)
          .province(province)
          .country(country)
          .created(now)
          .updated(now)
          .build();
      addressRepository.save(addressDTO);
      accountDTO.setAddress(addressDTO);
    }

    accountRepository.save(accountDTO);

    return accountDTO;
  }

  public Id<ExchangeId> saveExchange(Id<AccountId> id, ExchangeEnum exchange, String apiKey, String secret, Map<String, String> additionalParameters) throws JsonProcessingException {

    Preconditions.checkArgument(id != null, "AccountId can not be empty");
    Preconditions.checkArgument(exchange != null, "exchange can not be null");
    Preconditions.checkArgument(StringUtils.isNotEmpty(apiKey), "API Key can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(secret), "Secret can not be empty");
    Preconditions.checkArgument(! hasExchange(id, exchange), "Exchange " + exchange + "already exists for account");

    ExchangeConnectionParameters exchangeConnectionParameters = ExchangeConnectionFactory.build(exchange, apiKey, secret, additionalParameters);


    try {
      exchangeKeysValidatorService.checkKeys(exchangeConnectionParameters);
    } catch (IOException ioe) {
      throw new IllegalStateException("Failed to contact exchange", ioe);
    }

    final String encryptedApiKey = encryptionService.encryptSilent(apiKey, aesKey);
    final String encryptedSecret = encryptionService.encryptSilent(secret, aesKey);

    AccountDTO accountDTO = accountRepository.findOne(id);
    if (accountDTO.getExchanges() == null) {
      accountDTO.setExchanges(new ArrayList<>());
    }

    Date now = new Date();
    ExchangeDTO exchangeDTO = ExchangeDTO.builder()
        .id(new Id<>())
        .account(accountDTO)
        .apiKey(encryptedApiKey)
        .secret(encryptedSecret)
        .encryptionDate(new Date())
        .encryptionType(EncryptionType.AES_TYPE_1)
        .encryptionAlias(ENCRYPTION_ALIAS.ALIAS_ONE.name())
        .additional(additionalParameters)
        .exchange(Exchange.valueOf(exchange.name()))
        .created(now)
        .updated(now)
        .build();
    ExchangeDTO exchangeDTOsaved = exchangeRepository.save(exchangeDTO);

    accountDTO.getExchanges().add(exchangeDTO);
    accountRepository.save(accountDTO);

    return exchangeDTOsaved.getId();
  }

  public Optional<SecureExchangeInformation> findExchangeKeys(Id<AccountId> accountId, ExchangeEnum exchange) {

    Preconditions.checkArgument(accountId != null, "AccountId can not be empty");
    Preconditions.checkArgument(exchange != null, "exchange can not be null");

    AccountDTO accountDTO = accountRepository.findOne(accountId);

    Optional<ExchangeDTO> persistedExchange = accountDTO.getExchanges().stream()
        .filter( exchangeDTO -> exchangeDTO.getExchange().equals(Exchange.valueOf(exchange.name())) )
        .findAny();


    //TODO remove once all are converted
    //Update clear text values to encrypted
    if (persistedExchange.isPresent() && persistedExchange.get().getEncryptionType() == EncryptionType.CLEAR_TEXT) {
      final String encryptedApiKey = encryptionService.encryptSilent(persistedExchange.get().getApiKey(), aesKey);
      final String encryptedSecret = encryptionService.encryptSilent(persistedExchange.get().getSecret(), aesKey);

      ExchangeDTO exchangeDTO = persistedExchange.get();
      exchangeDTO.setApiKey(encryptedApiKey);
      exchangeDTO.setSecret(encryptedSecret);
      exchangeDTO.setEncryptionType(EncryptionType.AES_TYPE_1);
      exchangeDTO.setEncryptionDate(new Date());
      exchangeDTO.setEncryptionAlias(ENCRYPTION_ALIAS.ALIAS_ONE.name());

      exchangeRepository.saveAndFlush(exchangeDTO);
    }

    return persistedExchange.isPresent() ?
        Optional.of(SecureExchangeInformation.builder()
            .apiKey( persistedExchange.get().getEncryptionType() == EncryptionType.AES_TYPE_1 ?
                encryptionService.decryptSilent(persistedExchange.get().getApiKey(), aesKey) :
                persistedExchange.get().getApiKey() )
            .secret( persistedExchange.get().getEncryptionType() == EncryptionType.AES_TYPE_1 ?
                encryptionService.decryptSilent(persistedExchange.get().getSecret(), aesKey) :
                persistedExchange.get().getSecret() )
            .additional(persistedExchange.get().getAdditional())
            .build()):
        Optional.empty();
  }

  public void deleteExchange(Id<ExchangeId> exchangeId) {

    Preconditions.checkArgument(exchangeId != null, "exchangeId can not be empty");
    exchangeRepository.delete(exchangeId);
  }

  public boolean hasExchange(Id<AccountId> id, ExchangeEnum exchange) {
    Preconditions.checkArgument(id != null, "AccountId can not be empty");
    Preconditions.checkArgument(exchange != null, "exchange can not be null");

    AccountDTO accountDTO = accountRepository.findOne(id);
    Preconditions.checkArgument(accountDTO != null, "No account for id [" + id + "]");
    //Verify exchange doesn't exist already on account
    Optional<ExchangeDTO> existingExchange = accountDTO.getExchanges().stream()
        .filter( input -> input.getExchange().name().equals(exchange.name()))
        .findAny();

    return existingExchange.isPresent();
  }

  public Id<EmailSubscriptionId> saveEmailSubscription(Id<AccountId> id, EmailSubscription emailSubscription) {

    AccountDTO accountDTO = accountRepository.findOne(id);

    //TODO Verify subscription doesn't exist already on account

    if (accountDTO.getEmailSubscriptions() == null) {
      accountDTO.setEmailSubscriptions(new ArrayList<>());
    }

    Date now = new Date();
    EmailSubscriptionDTO emailSubscriptionDTO = EmailSubscriptionDTO.builder()
        .id(new Id<>())
        .account(accountDTO)
        .type(EmailSubscriptionType.valueOf(emailSubscription.getType().name()))
        .created(now)
        .updated(now)
        .build();

    accountDTO.getEmailSubscriptions().add(
        emailSubscriptionDTO
    );

    emailSubscriptionDTO = emailSubscriptionRepository.save(emailSubscriptionDTO);
    accountRepository.save(accountDTO);

    return emailSubscriptionDTO.getId();
  }

  public void removeEmailSubscription(Id<EmailSubscriptionId> subscriptionId) {

    Preconditions.checkArgument(subscriptionId != null, "subscriptionId can not be empty");

    emailSubscriptionRepository.delete(subscriptionId);
  }

  @Override
  public void afterPropertiesSet() {
    init();
  }


  @Builder
  @Getter
  public static final class SecureExchangeInformation {

    private String apiKey;
    private String secret;
    private Map<String, String> additional;

  }

  public enum ENCRYPTION_ALIAS {

    ALIAS_ONE,

  }


}
