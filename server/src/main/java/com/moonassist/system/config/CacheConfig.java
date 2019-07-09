package com.moonassist.system.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;
import com.moonassist.service.TickerService;
import com.moonassist.service.exchange.connection.ExchangeService;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {

    GuavaCacheManager cacheManager = new GuavaCacheManager(TickerService.CACHE_KEY, ExchangeService.CACHE_KEY);

    CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder()
        .maximumSize(10)
        .expireAfterWrite(5, TimeUnit.MINUTES);

    cacheManager.setCacheBuilder(cacheBuilder);

    return cacheManager;
  }

}