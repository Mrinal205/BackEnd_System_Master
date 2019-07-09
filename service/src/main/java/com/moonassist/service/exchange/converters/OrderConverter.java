package com.moonassist.service.exchange.converters;

import java.math.BigDecimal;
import java.util.UUID;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.order.OrderDTO;
import com.moonassist.type.Id;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;

import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderStatus;
import com.moonassist.bind.order.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(OrderConverter.class);


  public static Order convert(final OrderDTO orderDTO) {

    return Order.builder()
        .symbolPair(orderDTO.getSymbolPair())
        .exchangeOrderId(orderDTO.getExchangeOrderId())
        .offerType( orderDTO.getOffer() == com.moonassist.persistence.order.OfferType.SELL ? OfferType.SELL : OfferType.BUY)
        .orderType(orderDTO.getType() == com.moonassist.persistence.order.OrderType.MARKET ? OrderType.MARKET : OrderType.LIMIT)
        .price(orderDTO.getPrice())
        .amount(orderDTO.getAmount())
        .timestamp(orderDTO.getCreated())
        .status( convert(orderDTO.getStatus()) )
        .build();
  }

  public static OrderStatus convert(com.moonassist.persistence.order.OrderStatus status) {


    switch(status) {

      case CANCELED:
        return OrderStatus.CANCELED;

      case SUBMITTED:
        return OrderStatus.OPEN;

      case FULLFILLED:
        return OrderStatus.TRADED;

      case CREATED:
        return OrderStatus.OPEN;

      case FAILED:
        return OrderStatus.CANCELED;

      default:
        LOGGER.warn("No value for [" + status + "] in OrderStatus" );
        return null;

    }

  }

  public static Order convert(LimitOrder limitOrder) {

    return Order.builder()
        .symbolPair(limitOrder.getCurrencyPair().toString())
        .exchangeOrderId(limitOrder.getId())
        .offerType(convert(limitOrder.getType()))
        .orderType(OrderType.LIMIT)
        .price(limitOrder.getLimitPrice().compareTo(BigDecimal.ZERO) == 0 ? limitOrder.getAveragePrice() : limitOrder.getLimitPrice())
        .filled(limitOrder.getCumulativeAmount())
        .amount(limitOrder.getOriginalAmount())
        .timestamp(limitOrder.getTimestamp())
        .status(status(limitOrder.getOriginalAmount(), limitOrder.getCumulativeAmount()))
        .build();
  }

  public static OrderStatus status(BigDecimal originalAmount, BigDecimal filled) {
    if (BigDecimal.ZERO.compareTo(filled) == 0) {
      return OrderStatus.OPEN;
    }

    if (originalAmount.compareTo(filled) == 0) {
      return OrderStatus.TRADED;
    }

    return OrderStatus.TRADED_PARTIAL;
  }

  public static Order convert(UserTrade userTrade) {

    return Order.builder()
        .exchangeOrderId(userTrade.getOrderId())
        .offerType(convert(userTrade.getType()))
        .amount(userTrade.getOriginalAmount())
        .filled(userTrade.getOriginalAmount())
        .price(userTrade.getPrice().compareTo(BigDecimal.ZERO) == 0 ? userTrade.getOriginalAmount() : userTrade.getPrice())
        .symbolPair(userTrade.getCurrencyPair().toString())
        .timestamp(userTrade.getTimestamp())
        .status(OrderStatus.TRADED)
        .build();
  }

  public static OfferType convert(org.knowm.xchange.dto.Order.OrderType orderType) {

    return (orderType == org.knowm.xchange.dto.Order.OrderType.BID) ?
        OfferType.BUY :
        OfferType.SELL;
  }

  public static com.moonassist.persistence.order.OrderStatus orderStatus(final OrderStatus orderStatus) {

    switch ( orderStatus ) {

      case OPEN:
        return com.moonassist.persistence.order.OrderStatus.SUBMITTED;

      case TRADED:
        return com.moonassist.persistence.order.OrderStatus.FULLFILLED;

      case TRADED_PARTIAL:
        return com.moonassist.persistence.order.OrderStatus.SUBMITTED;

      case CANCELED:
        return com.moonassist.persistence.order.OrderStatus.CANCELED;

      default:
        throw new RuntimeException("Failed to map " + orderStatus );
    }

  }

  public static OrderDTO convert(final Order order, final AccountDTO accountDTO) {

    return OrderDTO.builder()
        .id(Id.from(UUID.randomUUID().toString()))
        .price(order.getPrice())
        .symbolPair(order.getSymbolPair())
        .exchangeOrderId(order.getExchangeOrderId())
        .account(accountDTO)
        .created(order.getTimestamp())
        .amount(order.getAmount())
        .exchange(Exchange.BINANCE)
        .type(order.getOrderType() == OrderType.LIMIT ? com.moonassist.persistence.order.OrderType.LIMIT : com.moonassist.persistence.order.OrderType.MARKET)
        .status(orderStatus(order.getStatus()))
        .offer((order.getOfferType() == OfferType.BUY) ? com.moonassist.persistence.order.OfferType.BUY : com.moonassist.persistence.order.OfferType.SELL )
        .build();
  }

}
