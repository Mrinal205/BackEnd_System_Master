package com.moonassist.service.exchange.binance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.moonassist.bind.order.OfferType;
import com.moonassist.bind.order.Order;
import com.moonassist.persistence.account.Exchange;
import com.moonassist.persistence.order.OrderDTO;
import com.moonassist.persistence.order.OrderRepository;
import com.moonassist.persistence.order.OrderStatus;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.UserService;
import com.moonassist.service.bean.RequestDetail;
import com.moonassist.service.bind.binance.ExchangeInfo;
import com.moonassist.service.bind.binance.Filter;
import com.moonassist.service.bind.binance.Symbol;
import com.moonassist.service.exception.OrderValidationException;
import com.moonassist.service.exchange.ExchangeOrderService;
import com.moonassist.service.util.RoundingUtil;
import com.moonassist.service.util.SymbolUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.knowm.xchange.binance.dto.BinanceException;
import org.knowm.xchange.binance.dto.trade.BinanceOrder;
import org.knowm.xchange.binance.dto.trade.BinanceTrade;
import org.knowm.xchange.binance.service.BinanceCancelOrderParams;
import org.knowm.xchange.binance.service.BinanceQueryOrderParams;
import org.knowm.xchange.binance.service.BinanceTradeService;
import org.knowm.xchange.binance.service.BinanceTradeServiceRaw;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Order Service for Binance. Where calls differ from other exchanges.
 *
 * API Information : https://github.com/binance-exchange/binance-official-api-docs/blob/master/rest-api.md#filters
 *
 */
@Slf4j
@Service
public class BinanceOrderService extends ExchangeOrderService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BinanceOrderService.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Long REC_WINDOW = 10000000L;

  private MathContext MATH_CONTEXT = new MathContext(8, RoundingMode.HALF_UP);

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private BinanceOrderConverter binanceOrderConverter;

  protected static Set<Object> CLOSED_STATUS =
      ImmutableSet.of(
          org.knowm.xchange.dto.Order.OrderStatus.FILLED,
          org.knowm.xchange.dto.Order.OrderStatus.CANCELED,
          org.knowm.xchange.dto.Order.OrderStatus.EXPIRED,
          org.knowm.xchange.dto.Order.OrderStatus.REJECTED,
          OrderStatus.CANCELED,
          OrderStatus.FULLFILLED,
          OrderStatus.FAILED,
          com.moonassist.bind.order.OrderStatus.CANCELED,
          com.moonassist.bind.order.OrderStatus.TRADED,
          org.knowm.xchange.binance.dto.trade.OrderStatus.CANCELED,
          org.knowm.xchange.binance.dto.trade.OrderStatus.FILLED,
          org.knowm.xchange.binance.dto.trade.OrderStatus.REJECTED,
          org.knowm.xchange.binance.dto.trade.OrderStatus.EXPIRED
          );

  @Override
  public List<Order> findClosedOrders(final RequestDetail requestDetail) {

    UserDTO userDTO = userService.find(requestDetail.getUserId());

    List<OrderDTO> orders = orderRepository.findAllByAccountAndExchange(userDTO.getAccount(), Exchange.BINANCE);

    return orders.stream()
        .filter( order -> CLOSED_STATUS.contains(order.getStatus()) )
        .map( order -> binanceOrderConverter.convert(order) )
        .collect(Collectors.toList());
  }

  @Override
  public List<Order> findClosedOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    BinanceTradeServiceRaw binanceTradeServiceRaw = (BinanceTradeServiceRaw) requestDetail.forRequest().getTradeService();

    CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    List<BinanceOrder> orders = binanceTradeServiceRaw.allOrders(currencyPair, null, null, REC_WINDOW, System.currentTimeMillis() );

    List<Order> result = orders.stream()
        .filter( order -> CLOSED_STATUS.contains(order.status) )
        .map( order -> {
          Optional<BigDecimal> averageTradePrice = (order.type != org.knowm.xchange.binance.dto.trade.OrderType.LIMIT) ?
              Optional.of(fetchAverageTrade(requestDetail, symbolPair, order.orderId)) :
              Optional.empty();

          return binanceOrderConverter.convert(order, averageTradePrice);
        })
        .collect( Collectors.toList() );

    return result;
  }

  @Override
  @SneakyThrows
  public List<Order> findOpenOrders(final RequestDetail requestDetail) {

    BinanceTradeServiceRaw binanceTradeServiceRaw = (BinanceTradeServiceRaw) requestDetail.forRequest().getTradeService();

    List<BinanceOrder> orders = binanceTradeServiceRaw.openOrders(REC_WINDOW, System.currentTimeMillis());

    return orders.stream()
        .map( order -> binanceOrderConverter.convert(order, Optional.empty()) )
        .collect( Collectors.toList() );
  }


  @Override
  public List<Order> findAllOrders(final RequestDetail requestDetail, final String symbolPair) throws IOException {

    BinanceTradeServiceRaw binanceTradeServiceRaw = (BinanceTradeServiceRaw) requestDetail.forRequest().getTradeService();

    CurrencyPair currencyPair = buildCurencyPair(symbolPair);

    List<BinanceOrder> orders = binanceTradeServiceRaw.allOrders(currencyPair, null, null, REC_WINDOW, System.currentTimeMillis() );

    return orders.stream()
        .map( order -> {

            Optional<BigDecimal> averageTradePrice = (order.type != org.knowm.xchange.binance.dto.trade.OrderType.LIMIT) ?
              Optional.of(fetchAverageTrade(requestDetail, symbolPair, order.orderId)) :
              Optional.empty();

            return binanceOrderConverter.convert(order, averageTradePrice);
        })
        .collect( Collectors.toList() );
  }


  //TODO this makes a lot of calls.. Need to trim this down.
  @SneakyThrows
  private BigDecimal fetchAverageTrade(final RequestDetail requestDetail, final String symbolPair, final Long orderId) {

    Preconditions.checkArgument(requestDetail != null, "requestDetail can not be null");
    Preconditions.checkArgument(StringUtils.isNotEmpty(symbolPair), "symbolPair can not be empty");
    Preconditions.checkArgument(orderId != null, "orderId can not be null");

    BinanceTradeServiceRaw tradeServiceRaw = (BinanceTradeServiceRaw) requestDetail.forRequest().getTradeService();
    CurrencyPair currencyPair = buildCurencyPair(symbolPair);
    List<BinanceTrade> trades = tradeServiceRaw.myTrades(currencyPair, null, null, REC_WINDOW, System.currentTimeMillis() );

    List<BinanceTrade> filteredTrades = trades.stream()
        .filter(trade -> trade.orderId == orderId)
        .collect(Collectors.toList());

    BigDecimal result = BigDecimal.ZERO;

    for (BinanceTrade trade : filteredTrades) {
      result = result.add(trade.price);
    }

    return (filteredTrades.size() > 0) ?
        result.divide(new BigDecimal(filteredTrades.size()), MATH_CONTEXT) :
        result;
  }

  @Override
  public Order placeOrder(final RequestDetail requestDetail, final Order order) {
    try {
      return super.placeOrder(requestDetail, order);
    } catch (BinanceException be) {

      if (be.msg.contains("MIN_NOTIONAL")) {
        throw new OrderValidationException("Order does not meet the minimal Price * Amount. Please increase your amount or price and try again");
      }

      if (be.msg.contains("PRICE_FILTER")) {
        throw new OrderValidationException("Order does not meet the minimal or maximum price. Please increase your price and try again");
      }

      throw new OrderValidationException(be.msg);
    }

  }


  public Order getOrder(final RequestDetail requestDetail, final String symbolPair, final String orderId) throws IOException {

    BinanceTradeService tradeService = (BinanceTradeService) requestDetail.forRequest().getTradeService();

    BinanceQueryOrderParams binanceQueryOrderParams = new BinanceQueryOrderParams();
    binanceQueryOrderParams.setOrderId(orderId);
    binanceQueryOrderParams.setCurrencyPair(new CurrencyPair(symbolPair));


    Collection<org.knowm.xchange.dto.Order> orders = tradeService.getOrder(binanceQueryOrderParams);



    Preconditions.checkState(orders.size() == 1, "Found " + orders.size() + " when expected 1");

    org.knowm.xchange.dto.Order order = Lists.newArrayList(orders).get(0);

    return Order.builder()
        .amount(order.getOriginalAmount())
        .symbolPair(order.getCurrencyPair().toString())
        .build();
  }


  @Override
  protected MarketOrder buildMarket(final RequestDetail requestDetail, final Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    ExchangeInfo exchangeInformation = exchangeInformation();
    final Symbol symbol = exchangeInformation.symbolsMap().get(SymbolUtils.just(order.getSymbolPair()));
    final BigDecimal amount = massageAmount(symbol, order.getAmount());

    return new MarketOrder.Builder(orderType, currencyPair)
        .originalAmount(amount)
        .build();
  }

  @Override
  protected LimitOrder buildLimit(final RequestDetail requestDetail, final Order order) {

    CurrencyPair currencyPair = buildCurencyPair(order.getSymbolPair());

    //XChange's order type is our offer type --
    org.knowm.xchange.dto.Order.OrderType orderType = order.getOfferType() == OfferType.BUY ?
        org.knowm.xchange.dto.Order.OrderType.BID :
        org.knowm.xchange.dto.Order.OrderType.ASK;

    ExchangeInfo exchangeInformation = exchangeInformation();
    final Symbol symbol = exchangeInformation.symbolsMap().get(SymbolUtils.just(order.getSymbolPair()));
    if (symbol == null) {
      LOGGER.warn("Missing exchange info for Binance " + order.getSymbolPair());
    }

    final BigDecimal amount = massageAmount(symbol, order.getAmount());

    return new LimitOrder.Builder(orderType, currencyPair)
        .limitPrice(order.getPrice())
        .originalAmount(amount)
        .build();
  }

  private BigDecimal massageAmount(final Symbol symbol, final BigDecimal amount) {
    if (symbol == null) {
      return amount;
    }

    Filter filter = symbol.filterMap().get(Filter.FilterType.LOT_SIZE);

    Preconditions.checkArgument(amount.compareTo(filter.getMinQty()) >= 0, "Minimum order size: " + filter.getMinQty().stripTrailingZeros());
    Preconditions.checkArgument(amount.compareTo(filter.getMaxQty()) <= 0, "Maximum order size: " + filter.getMaxQty().stripTrailingZeros());

    return RoundingUtil.round(amount, RoundingUtil.places(filter.getStepSize()), RoundingMode.DOWN);
  }


  /**
   * Results from calling `https://www.binance.com/api/v1/exchangeInfo`
   * @return
   * @throws IOException
   */
  @SneakyThrows
  protected ExchangeInfo exchangeInformation() {

    //TODO make this part of the singleton

    String file = new String(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("binance.exchange.json")));
    Preconditions.checkState(StringUtils.isNotEmpty(file), "Error looking up Binance Exchange Info File");

    return MAPPER.readValue(file, ExchangeInfo.class);
  }

  @Override
  public boolean cancelOrder(final RequestDetail requestDetail, final String exchangeOrderId, final String symbolPair) throws IOException {

    CurrencyPair currencyPair = buildCurencyPair(symbolPair);
    BinanceCancelOrderParams binanceCancelOrderParams = new BinanceCancelOrderParams(currencyPair, exchangeOrderId);

    return requestDetail.forRequest().getTradeService().cancelOrder(binanceCancelOrderParams);
  }

}
