package com.moonassist.system.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.util.WebUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moonassist.bind.account.Account;
import com.moonassist.bind.account.Address;
import com.moonassist.bind.account.EmailSubscription;
import com.moonassist.bind.account.Exchange;
import com.moonassist.service.AccountService;
import com.moonassist.service.AuthenticationService;
import com.moonassist.service.EventsService;
import com.moonassist.service.UserService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.system.security.SecurityConstants;
import com.moonassist.type.EmailSubscriptionId;
import com.moonassist.type.ExchangeId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/accounts")
public class AccountController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

  @Autowired
  private TokenService tokenService;

  @Autowired
  private UserService userService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private EventsService eventsService;

  @Autowired
  private AuthenticationService authenticationService;


  //TODO Better tenant check, right now it is in the service.
  @ResponseBody
  @RequestMapping(value = "/{id}", method = RequestMethod.GET)
  ResponseEntity<Account> get(@PathVariable("id") String id, HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException {
    LOGGER.debug("Fetching account data for [" + id + "]");

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);
    Account account = accountService.find(Id.from(id), userId);
    return new ResponseEntity<>(account, HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
  ResponseEntity<Void> put(@PathVariable("id") String id, @RequestBody Account account) {

    account.validate();
    accountService.update(Id.from(id), account);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /************************* Address ********************************/


  //TODO verify account belongs to user
  @ResponseBody
  @RequestMapping(value = "/{id}/address", method = RequestMethod.PUT)
  ResponseEntity<Address> updateAddress(@PathVariable("id") String id, @RequestBody Address address) {


    //Validation
    address.validate();

    accountService.saveAddress(Id.from(id), address.line1, address.line2, address.city, address.province, address.postal, address.country);

    return new ResponseEntity<>(address, HttpStatus.OK);
  }


  /************************* Exchanges***** **************************/


  //TODO verify account belongs to user
  @ResponseBody
  @RequestMapping(value = "/{id}/exchanges", method = RequestMethod.POST)
  ResponseEntity<Exchange> saveExchange(@PathVariable("id") String id, @RequestBody Exchange exchange) throws JsonProcessingException {

    //Validation
    exchange.validate();

    Id<ExchangeId> exchangeId = accountService.saveExchange(Id.from(id), exchange.exchangeName, exchange.apiKey, exchange.secret, exchange.additional);

    exchange.mask();
    exchange.id = exchangeId.value();
    return new ResponseEntity<>(exchange, HttpStatus.OK);
  }


  //TODO verify account belongs to user
  @ResponseBody
  @RequestMapping(value = "/{id}/exchanges/{exchangeId}", method = RequestMethod.DELETE)
  ResponseEntity<Void> removeExchange(@PathVariable("id") String id, @PathVariable("exchangeId") String exchangeId) {

    accountService.deleteExchange(Id.from(exchangeId));

    return new ResponseEntity<>(HttpStatus.OK);
  }

  /************************* Email Subscription **************************/

  //TODO verify account belongs to user
  @ResponseBody
  @RequestMapping(value = "/{id}/emailsubscriptions", method = RequestMethod.POST)
  ResponseEntity<EmailSubscription> saveEmailSubcription(@PathVariable("id") String id, @RequestBody EmailSubscription emailSubscription) {

    //Validation
    emailSubscription.validate();

    Id<EmailSubscriptionId> saveEmailSubscriptionId = accountService.saveEmailSubscription(Id.from(id), emailSubscription);
    emailSubscription.setId(saveEmailSubscriptionId.value());
    return new ResponseEntity<>(emailSubscription, HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(value = "/{id}/emailsubscriptions/{subscriptionId}", method = RequestMethod.DELETE)
  ResponseEntity<Void> removeEmailSubcription(@PathVariable("id") String id, @PathVariable("subscriptionId") String subscriptionId) {

    accountService.removeEmailSubscription(Id.from(subscriptionId));

    return new ResponseEntity<>(HttpStatus.OK);
  }

}
