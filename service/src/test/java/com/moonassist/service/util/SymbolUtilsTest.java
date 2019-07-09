package com.moonassist.service.util;

import org.junit.Assert;
import org.junit.Test;

public class SymbolUtilsTest {

  @Test
  public void testJust() {

    Assert.assertEquals("ETHBTC", SymbolUtils.just("ETH/BTC"));
    Assert.assertEquals("ETHBTC", SymbolUtils.just("ETH-BTC"));

  }

}
