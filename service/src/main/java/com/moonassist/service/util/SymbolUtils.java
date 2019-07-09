package com.moonassist.service.util;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;

public class SymbolUtils {

  /**
   * Just the symbols, no separator.
   *
   * @param symbolPair
   * @return
   */
  public static String just(String symbolPair) {

    return StringUtils.replaceAll(symbolPair, "/|-", "");

  }

  /**
   * Returns the first currency or Base in the symbolPair
   * @param symbolPair
   * @return
   */
  public static String base(String symbolPair) {

    return Splitter.on('/').split(symbolPair).iterator().next();
  }

  /**
   * Returns the first currency or Base in the symbolPair
   * @param symbolPair
   * @return
   */
  public static String count(String symbolPair) {

    Iterator<String> values = Splitter.on('/').split(symbolPair).iterator();
    values.next();
    return values.next();
  }


}
