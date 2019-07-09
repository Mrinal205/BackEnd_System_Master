package com.moonassist.service.exchange.binance;

import com.google.common.annotations.VisibleForTesting;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.bind.order.OrderStatus;
import com.moonassist.bind.order.OrderType;
import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.order.OrderDTO;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.type.Id;
import org.knowm.xchange.binance.BinanceAdapters;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.OrderSide;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class BinanceOrderConverter {

  private static final Logger LOGGER = LoggerFactory.getLogger(BinanceOrderConverter.class);


  public Order convert(final OrderDTO orderDTO) {

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

  public OrderStatus convert(com.moonassist.persistence.order.OrderStatus status) {


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

  public Order convert(LimitOrder limitOrder) {

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

  public OrderStatus status(BigDecimal originalAmount, BigDecimal filled) {
    if (BigDecimal.ZERO.compareTo(filled) == 0) {
      return OrderStatus.OPEN;
    }

    if (originalAmount.compareTo(filled) == 0) {
      return OrderStatus.TRADED;
    }

    return OrderStatus.TRADED_PARTIAL;
  }

  public Order convert(UserTrade userTrade) {

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

  public OfferType convert(org.knowm.xchange.dto.Order.OrderType orderType) {

    return (orderType == org.knowm.xchange.dto.Order.OrderType.BID) ?
        OfferType.BUY :
        OfferType.SELL;
  }

  public com.moonassist.persistence.order.OrderStatus orderStatus(final OrderStatus orderStatus) {

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

  //Could use BinanceAdapter to convert to xknown type Order if needed
  @VisibleForTesting
  protected Order convert(final BinanceOrder binanceOrder, final Optional<BigDecimal> averageTradePrice) {

    return Order.builder()
        .amount(binanceOrder.origQty)
        .filled(binanceOrder.executedQty)
        .exchangeName(ExchangeEnum.BINANCE)
        .exchangeOrderId("" + binanceOrder.orderId)
        .offerType((binanceOrder.side == OrderSide.BUY) ? OfferType.BUY : OfferType.SELL)
        .orderType( binanceOrder.type == org.knowm.xchange.binance.dto.trade.OrderType.LIMIT ? com.moonassist.bind.order.OrderType.LIMIT : com.moonassist.bind.order.OrderType.MARKET)
        .price( (averageTradePrice.isPresent()) ? averageTradePrice.get() : binanceOrder.price )
        .status(convert(binanceOrder.status))
        .timestamp(binanceOrder.getTime())
        .symbolPair(BinanceAdapters.adaptSymbol(binanceOrder.symbol).toString())
        .build();
  }

  public OrderStatus convert( org.knowm.xchange.binance.dto.trade.OrderStatus orderStatus) {

    switch (orderStatus) {

      case NEW:
        return OrderStatus.OPEN;
      case FILLED:
        return OrderStatus.TRADED;
      case CANCELED:
        return OrderStatus.CANCELED;
      case PARTIALLY_FILLED:
        return OrderStatus.TRADED_PARTIAL;
      case EXPIRED:
        return OrderStatus.CANCELED;
      case REJECTED:
        return OrderStatus.CANCELED;
      case PENDING_CANCEL:
        return OrderStatus.CANCELED;

      default:
        throw new RuntimeException("No mapped status for [" + orderStatus + "]");
    }

  }


}
