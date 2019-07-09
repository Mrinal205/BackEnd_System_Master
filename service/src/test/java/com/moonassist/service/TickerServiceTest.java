package com.moonassist.service;

import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.moonassist.bind.account.ExchangeEnum;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TickerServiceTest.PropertyConfig.class, TickerService.class})
@ActiveProfiles("binance")
public class TickerServiceTest {


  @Autowired
  private TickerService tickerService;

  @Test
  public void testGetTickers() {

    Map<String, TickerService.Ticker> tickers = tickerService.fetchTickers(ExchangeEnum.BINANCE);

    Assert.assertNotNull(tickers);
    Assert.assertFalse(tickers.isEmpty());
    System.out.println(tickers);
  }

  @Configuration
  public static class PropertyConfig {

    @Bean
    public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {

      PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
      final Properties properties = new Properties();
      properties.setProperty("ticker.url", "https://test-moonassist-backend-ticker.herokuapp.com");
      ppc.setProperties(properties);

      return ppc;
    }
  }

}
