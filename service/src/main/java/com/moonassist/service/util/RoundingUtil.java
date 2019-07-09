package com.moonassist.service.util;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingUtil {

  private static final Integer MAX_DECMIAL_PLACES = 10;

  public static double round(final double value, final int places) {

    Preconditions.checkArgument(places >= 0, "decimal place can not be less than 0");

    BigDecimal bd = new BigDecimal(Double.toString(value));
    bd = bd.setScale(places, RoundingMode.HALF_UP);
    return bd.doubleValue();
  }

  public static BigDecimal round(final BigDecimal bigDecimal, final int places, final RoundingMode roundingMode) {

    Preconditions.checkArgument(places >= 0, "decimal place can not be less than 0");

    return bigDecimal.setScale(places, roundingMode);
  }


  public static Integer places(final BigDecimal bigDecimal) {

    for (int i = 0; i < MAX_DECMIAL_PLACES; i++) {
      BigDecimal multiplier = BigDecimal.TEN.pow(i);

      if (bigDecimal.multiply(multiplier).compareTo( BigDecimal.ONE ) == 0) {
        return i;
      }

    }

    throw new RuntimeException("places was larger than " + MAX_DECMIAL_PLACES);
  }

}
