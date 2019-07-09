package com.moonassist.service.config;


import com.moonassist.persistence.account.AccountRepository;
import com.moonassist.persistence.account.AddressRepository;
import com.moonassist.persistence.account.EmailSubscriptionRepository;
import com.moonassist.persistence.account.ExchangeRepository;
import com.moonassist.persistence.user.EventRepository;
import com.moonassist.persistence.user.TwofactorRepository;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.AccountService;
import com.moonassist.service.EventsService;
import com.moonassist.service.TimeService;
import com.moonassist.service.UserService;
import com.moonassist.service.authentication.TimeBased2FA;
import com.moonassist.service.authentication.TwoFactorAuthenticationService;
import com.moonassist.service.email.EmailService;
import com.moonassist.service.email.ForgotPasswordEmailService;
import com.moonassist.service.email.templates.TemplateFactory;
import com.moonassist.service.exchange.ExchangeSpecificationFactory;
import com.moonassist.service.exchange.connection.ExchangeService;
import com.moonassist.service.exchange.binance.BinanceOrderService;
import com.moonassist.service.exchange.bittrex.BittrexOrderService;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.exchange.ExchangeOrderServiceFactory;
import com.moonassist.service.exchange.coinbasepro.GDAXOrderService;
import com.moonassist.service.exchange.kucoin.KucoinOrderService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    ExchangeService.class,
    ExchangeOrderServiceFactory.class,
    ExchangeOrderService.class,
    BittrexOrderService.class,
    KucoinOrderService.class,
    BinanceOrderService.class,
    GDAXOrderService.class,
    UserService.class,
    ForgotPasswordEmailService.class,
    EmailService.class,
    TemplateFactory.class,
    TimeService.class,
    ExchangeSpecificationFactory.class,
    AccountService.class,
    EventsService.class,
    TwoFactorAuthenticationService.class,
    TimeBased2FA.class,
    ExchangeService.class
})
public class IntegrationConfiguration {

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private AccountRepository accountRepository;

  @MockBean
  private AddressRepository addressRepository;

  @MockBean
  private ExchangeRepository exchangeRepository;

  @MockBean
  private EventRepository eventRepository;

  @MockBean
  private TwofactorRepository twofactorRepository;

  @MockBean
  private EmailSubscriptionRepository emailSubscriptionRepository;

}
