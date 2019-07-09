package com.moonassist.service;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class FraudService {

  private static List<String> IGNORED_IPS = ImmutableList.of(
      "127.0.0.1"
  );

  private Cache<String, LoginCacheEvent> LOGIN_CACHE_EMAIL = CacheBuilder.newBuilder()
      .maximumSize(10000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  private Cache<String, LoginCacheEvent> LOGIN_CACHE_IP = CacheBuilder.newBuilder()
      .maximumSize(10000)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build();

  public void checkEmailCache(final String email, final Long maxAttempts) {
    Preconditions.checkArgument(StringUtils.isNoneEmpty(email), "email can not be empty");

    checkAndPutCache(LOGIN_CACHE_EMAIL, email, maxAttempts);
  }

  public void checkIPCache(final String ipAddress, final Long maxAttempts) {
    Preconditions.checkArgument(StringUtils.isNoneEmpty(ipAddress), "ipAddress can not be empty");
    if (IGNORED_IPS.contains(ipAddress)) {
      return;
    }

    checkAndPutCache(LOGIN_CACHE_IP, ipAddress, maxAttempts);
  }

  private void checkAndPutCache(final Cache<String, LoginCacheEvent> cache, final String key, final Long maxAttempts) {
    Preconditions.checkArgument(StringUtils.isNoneEmpty(key), "key can not be empty");

    LoginCacheEvent loginCacheEvent = cache.getIfPresent(key);

    if (loginCacheEvent != null) {
      loginCacheEvent.count ++;
      if (loginCacheEvent.count > maxAttempts) {
        log.info("Failing login for user [" + key + "] for max attempts " + loginCacheEvent);
      }
      Preconditions.checkState(loginCacheEvent.count < maxAttempts, "Max Attempts");
    }
    else {
      cache.put(key, LoginCacheEvent.builder()
          .count(0L)
          .last(new Date())
          .build()
      );

    }
  }

  @Builder
  @ToString
  public static class LoginCacheEvent {

    private Long count;
    private Date last;

  }

}
