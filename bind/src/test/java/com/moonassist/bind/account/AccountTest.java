package com.moonassist.bind.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class AccountTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testMarshalObject() throws JsonProcessingException {

    Account account = Account.builder()
        .personalDetails(
            PersonalDetails.builder()
                .name("John Doe")
                .dateOfBirth(new Date())
                .phone("01.801.555.3214")
                .build()
        )
        .address(
            Address.builder()
                .line1("Line1")
                .line2("Line2")
                .city("Some City")
                .province("Some Province")
                .country("USA")
                .build()
        )
        .id(RandomStringUtils.randomAlphabetic(10))
        .subscription(Subscription.builder()
            .subscriptionType(SubscriptionType.FREE)
            .startDate(DateUtils.addMonths(new Date(), -1))
            .build()
        )
        .exchanges(ImmutableSet.of(
            Exchange.builder()
                .apiKey("apiKey")
                .secret("secret")
                .exchangeName(ExchangeEnum.GDAX)
                .build()
        ))
        .twoFactorEnabled(true)
        .whiteListedIpAddress(ImmutableList.of("127.0.0.1", "192.168.1.1"))
        .build();

    Assert.assertNotNull(mapper.writeValueAsString(account));
  }

}
