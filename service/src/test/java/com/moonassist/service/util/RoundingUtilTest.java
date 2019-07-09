package com.moonassist.service.util;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingUtilTest {


  @Test
  public void testRound() {

    BigDecimal value = new BigDecimal("123.45697");
    BigDecimal expected = new BigDecimal("123.456");

    Assert.assertEquals(expected, RoundingUtil.round(value, 3, RoundingMode.DOWN));
  }

  @Test
  public void testRound_one() {

    BigDecimal value = new BigDecimal("20.3");
    BigDecimal expected = new BigDecimal("20");

    Assert.assertEquals(expected, RoundingUtil.round(value, 0, RoundingMode.DOWN));
  }

  @Test
  public void testPlaces() {

    Assert.assertEquals(Integer.valueOf(3), RoundingUtil.places(new BigDecimal("0.001")));

    Assert.assertEquals(Integer.valueOf(0), RoundingUtil.places(new BigDecimal("1.00")));


  }

}

