package com.moonassist.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.service.logging.CustomClientHttpRequestInterceptor;

import lombok.Data;
import lombok.ToString;

@Service
public class TickerService {

  public static final String CACHE_KEY = "tickerCache";

  @Value("${ticker.url}")
  private String tickerURI;

  @Cacheable(value = CACHE_KEY)
  public Map<String, Ticker> fetchTickers(final ExchangeEnum exchange) {

    RestTemplate restTemplate = new RestTemplateBuilder()
        .setConnectTimeout(1000)
        .setReadTimeout(10000)
        .additionalInterceptors(ImmutableList.of(new CustomClientHttpRequestInterceptor()))
        .build();

    ResponseEntity<TickerResponse> response  = restTemplate.getForEntity(tickerURI + "/tickers?exchange=" + exchange.name().toLowerCase(),  TickerResponse.class);

    return response.getBody().ticker;
  }

  @Data
  public static class Ticker {

    private String symbol;

    private BigDecimal price;

  }

  @Data
  @ToString
  public static class TickerResponse {

    private String exchange;

    private Map<String, Ticker> ticker;

  }

}
