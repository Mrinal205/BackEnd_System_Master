package com.moonassist.service;

import com.google.common.base.Preconditions;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.Orders;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.order.OfferType;
import com.moonassist.persistence.order.OrderDTO;
import com.moonassist.persistence.order.OrderRepository;
import com.moonassist.persistence.order.OrderStatus;
import com.moonassist.persistence.order.OrderType;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.exchange.connection.ExchangeService;
import com.moonassist.service.exchange.ExchangeOrderServiceFactory;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import com.moonassist.type.OrderId;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);

  @Autowired
  private ExchangeService exchangeService;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountService accountService;

  @Autowired
  private ExchangeOrderServiceFactory exchangeOrderServiceFactory;


  public List<Order> getOpenOrders(final RequestDetail requestDetail) {

    Preconditions.checkArgument(requestDetail.getExchangeEnum().isPresent(), "exchange is required");

    List<Order> orders = exchangeOrderServiceFactory.factory(requestDetail).findOpenOrders(requestDetail);

    return orders;
  }

  public Orders getOrders(final RequestDetail requestDetail) {

    List<Order> orders = exchangeOrderServiceFactory.factory(requestDetail)
        .findAllOrders(requestDetail);

    List<Order> openOrders = orders.stream()
        .filter( order -> order.getStatus() == com.moonassist.bind.order.OrderStatus.OPEN)
        .collect(Collectors.toList());

    List<Order> closedOrders = orders.stream()
        .filter( order -> order.getStatus() != com.moonassist.bind.order.OrderStatus.OPEN)
        .collect(Collectors.toList());

    return Orders.builder()
        .open(openOrders)
        .closed(closedOrders)
        .build();
  }

  public Orders getOrders(final RequestDetail requestDetail, String symbolPair) throws IOException {

    List<Order> orders = exchangeOrderServiceFactory.factory(requestDetail)
        .findAllOrders(requestDetail, symbolPair);

    List<Order> openOrders = orders.stream()
        .filter( order -> order.getStatus() == com.moonassist.bind.order.OrderStatus.OPEN)
        .collect(Collectors.toList());

    List<Order> closedOrders = orders.stream()
        .filter( order -> order.getStatus() != com.moonassist.bind.order.OrderStatus.OPEN)
        .collect(Collectors.toList());

    return Orders.builder()
        .open(openOrders)
        .closed(closedOrders)
        .build();
  }

  public Order placeOrder(final RequestDetail requestDetail, final Order order) {

    UserDTO userDTO = userRepository.findOne(requestDetail.getUserId());
    Id<AccountId> accountId = userDTO.getAccount().getId();
    boolean hasExchange = accountService.hasExchange(accountId, order.getExchangeName());
    Preconditions.checkArgument(hasExchange, "AccountId[" + accountId + "] does not have exchange " + order.getExchangeName());

    //Call Exchange
    //TODO Move this to an bootstrapping process
    Optional<AccountService.SecureExchangeInformation> exchangeDetails = accountService.findExchangeKeys(accountId, order.getExchangeName());
    Preconditions.checkArgument(exchangeDetails.isPresent(), "AccountId [" + accountId + "] does not have keys for Exchange [" + order.getExchangeName() + "]");

    //Create Unique OrderId, so it can be used for logging.
    Id<OrderId> orderId = new Id<>();
    order.setId(orderId.toString());

    final Order placedOrder = exchangeOrderServiceFactory.factory(requestDetail).placeOrder(requestDetail, order);

    LOGGER.info("Placed Order [" + order.getExchangeOrderId() + "] for accountId [" + accountId + "] userId [" + requestDetail.getUserId() + "]");

    OrderDTO createdOrder = create(requestDetail.getUserId(), orderId, order);
    createdOrder.setStatus(OrderStatus.SUBMITTED);
    createdOrder.setExchangeOrderId(placedOrder.getExchangeOrderId());
    createdOrder = orderRepository.save(createdOrder);

    placedOrder.setId(createdOrder.getId().toString());

    return placedOrder;
  }

  public boolean delete(final Id<UserId> userId,  Id<OrderId> orderId) throws IOException {

    OrderDTO orderDTO = orderRepository.findOne(orderId);
    ExchangeEnum exchangeEnum = ExchangeEnum.valueOf(orderDTO.getExchange().name());

    Optional<AccountService.SecureExchangeInformation> exchangeDetails = accountService.findExchangeKeys(orderDTO.getAccount().getId(), exchangeEnum);
    Preconditions.checkArgument(exchangeDetails.isPresent(), "AccountId [" + orderDTO.getAccount().getId() + "] does not have keys for Exchange [" + exchangeEnum + "]");

    LOGGER.info("Canceling Order Id [" + orderId + "] exchangeOrderId [" + orderDTO.getExchangeOrderId() + "]");

    RequestDetail requestDetail = RequestDetail.builder()
        .userId(userId)
        .exchangeEnum(exchangeEnum)
        .exchange(exchangeService.buildExchange(userId, exchangeEnum))
        .build();

    return exchangeOrderServiceFactory.factory(requestDetail)
        .cancelOrder(requestDetail, orderDTO.getExchangeOrderId(), orderDTO.getSymbolPair());
  }

  public ExchangeEnum getExchangeForOrder(Id<OrderId> orderId) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(orderId.value()), "OrderId can not be empty");

    OrderDTO orderDTO = orderRepository.findOne(orderId);
    Preconditions.checkArgument(orderDTO != null, orderId + " is not valid for an order");

    return ExchangeEnum.valueOf(orderDTO.getExchange().name());
  }

  public boolean delete(final RequestDetail requestDetail, Id<AccountId> accountId, String symbolPair, String exchangeOrderId) throws IOException {

    Optional<AccountService.SecureExchangeInformation> exchangeDetails = accountService.findExchangeKeys(accountId, requestDetail.getExchangeEnum().get());
    Preconditions.checkArgument(exchangeDetails.isPresent(), "AccountId [" + accountId + "] does not have keys for Exchange [" + requestDetail.getExchangeEnum().get() + "]");


    return exchangeOrderServiceFactory.factory(requestDetail)
        .cancelOrder(requestDetail, exchangeOrderId, symbolPair);
  }

  private OrderDTO create(Id<UserId> userId, Id<OrderId> orderId, Order order) {
    Preconditions.checkArgument(userId != null, "UserId can not be null");
    Preconditions.checkArgument(order != null, "Order can not be null");

    UserDTO userDTO = userRepository.findOne(userId);

    OrderDTO orderDTO = OrderDTO.builder()
        .id(orderId)
        .status(OrderStatus.CREATED)
        .account(userDTO.getAccount())
        .symbolPair(order.getSymbolPair())
        .amount(order.getAmount())
        .exchange(Exchange.valueOf(order.getExchangeName().name()))
        .price(order.getPrice())
        .type(OrderType.valueOf(order.getOrderType().name()))
        .offer(OfferType.valueOf(order.getOfferType().name()))
        .created(new Date())
        .exchangeOrderId(order.getExchangeOrderId())
        .build();

    return orderRepository.save(orderDTO);
  }

}
