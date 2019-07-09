package com.moonassist.service.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log.class);

  public static final void warnIf(boolean condition, String message) {

    if (condition) {
      LOGGER.warn(message);
    }

  }

}
