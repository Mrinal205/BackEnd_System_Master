package com.moonassist.system.controller;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.Orders;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.AuthenticationService;
import com.moonassist.service.OrderService;
import com.moonassist.service.UserService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.connection.ExchangeService;
import com.moonassist.system.security.SecurityConstants;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import java.io.IOException;

@CrossOrigin
@EnableWebMvc
@RestController
@RequestMapping("/orders")
public class OrderController extends BaseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

  @Autowired
  private TokenService tokenService;

  @Autowired
  private OrderService orderService;

  @Autowired
  private AuthenticationService authenticationService;

  @Autowired
  private UserService userService;

  @Autowired
  private ExchangeService exchangeService;


  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Order> create(@RequestBody Order order, HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException {

    LOGGER.info("Processing order [" + order + "]");

    order.validate();

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(order.getExchangeName().name()))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(order.getExchangeName().name())))
        .build();

    Order createdOrder = orderService.placeOrder(requestDetail, order);

    return new ResponseEntity<>(createdOrder, HttpStatus.OK);
  }

  @ResponseBody
  @RequestMapping(method = RequestMethod.GET, value = "/{exchange}")
  public ResponseEntity<Orders> fetchAllByExchange(@PathVariable final String exchange, @QueryParam("status") String status, HttpServletRequest httpServletRequest) throws AuthenticationService
      .AuthenticationException {

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    //Request Validation
    Preconditions.checkArgument(StringUtils.isNotEmpty(exchange), "exchange can not be empty");

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(exchange))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(exchange)))
        .build();

    Orders orders = orderService.getOrders(requestDetail);

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }

  @RequestMapping(value = "/{exchange}/{symbolPair}", method = RequestMethod.GET)
  public ResponseEntity<Orders> fetchAllByExchangeAndSymbolPair(@PathVariable final String exchange, @PathVariable final String symbolPair, final HttpServletRequest httpServletRequest) throws AuthenticationService
      .AuthenticationException, IOException {

    Preconditions.checkArgument(StringUtils.isNotEmpty(exchange), "Exchange can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(symbolPair), "SymbolPair can not be empty");
    ExchangeEnum exchangeEnum = ExchangeEnum.valueOf(exchange);
    Preconditions.checkArgument( exchangeEnum != null, "Exchange " + exchange + " is not valid");

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(exchange))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(exchange)))
        .build();

    //Needed because URI uses dash for symbol pair
    String convertedSymbolPair = symbolPair.replace("-", "/");

    Orders orders = orderService.getOrders(requestDetail, convertedSymbolPair);

    return new ResponseEntity<>(orders, HttpStatus.OK);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Order> delete(@PathVariable("id") String id, HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException, IOException {

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    orderService.delete(userId, Id.from(id));

    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @RequestMapping(value = "/{exchange}/{symbolPair}/{exchangeOrderId}", method = RequestMethod.DELETE)
  public void deleteByExchangeId(
      @PathVariable("exchange") String exchange,
      @PathVariable("symbolPair") String symbolPair,
      @PathVariable("exchangeOrderId") String exchangeOrderId,
      HttpServletRequest httpServletRequest) throws AuthenticationService.AuthenticationException, IOException {

    Preconditions.checkArgument(StringUtils.isNotEmpty(exchange), "Exchange can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(symbolPair), "SymbolPair can not be empty");
    ExchangeEnum exchangeEnum = ExchangeEnum.valueOf(exchange);
    Preconditions.checkArgument( exchangeEnum != null, "Exchange " + exchange + " is not valid");
    Preconditions.checkArgument(StringUtils.isNotEmpty(exchangeOrderId), "exchangeOrderId can not be empty");

    String token = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME).getValue();
    Id<UserId> userId = authenticationService.retrieveAuthentication(token);

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(ExchangeEnum.valueOf(exchange))
        .exchange(exchangeService.buildExchange(userId, ExchangeEnum.valueOf(exchange)))
        .build();

    String convertedSymbolPair = symbolPair.replace("-", "/");


    //TODO Move userId and Account into context of every request.

    final UserDTO userDTO = userService.find(userId);

    orderService.delete(requestDetail, userDTO.getAccount().getId(), convertedSymbolPair, exchangeOrderId);
  }

}
