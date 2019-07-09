package com.moonassist.persistence.order;

import com.moonassist.persistence.account.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.type.Id;
import com.moonassist.type.OrderId;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderDTO, Id<OrderId>> {

  OrderDTO findByAccountAndExchangeAndExchangeOrderId(AccountDTO accountDTO, Exchange exchange, String exchangeOrderId);

  List<OrderDTO> findAllByAccountAndExchange(AccountDTO accountDTO, Exchange exchange);

}