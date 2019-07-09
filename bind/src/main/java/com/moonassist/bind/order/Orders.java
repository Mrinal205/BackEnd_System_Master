package com.moonassist.bind.order;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
//@NoArgsConstructor
public class Orders {

  private List<Order> open;

  private List<Order> closed;

  @Builder
  public Orders(List<Order> open, List<Order> closed) {
    this.open = open;
    this.closed = closed;
  }

}
