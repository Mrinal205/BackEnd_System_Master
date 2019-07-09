package com.moonassist.bind.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;

public class OrderTest {

  private ObjectMapper mapper = new ObjectMapper();


  @Test
  public void testMarshalObject() throws JsonProcessingException {

    Order order = Order.builder()
        .amount(new BigDecimal("23.00"))
        .build();

    Assert.assertNotNull(mapper.writeValueAsString(order));
  }

  @Test
  public void testUnMarshalObject() throws IOException {

    BigDecimal price = new BigDecimal("295.68");
    BigDecimal amount = new BigDecimal("0.02474826");


    String json = "{\"symbolPair\":\"ETH/USD\",\"offerType\":\"SELL\",\"orderType\":\"LIMIT\",\"exchangeName\":\"GDAX\",\"amount\":0.02474826,\"price\":" + price +"}";

    Order order = mapper.readValue(json, Order.class);

    Assert.assertNotNull(order);
    Assert.assertEquals(order.getPrice(), price);
    Assert.assertEquals(order.getAmount(), amount);
  }

}
