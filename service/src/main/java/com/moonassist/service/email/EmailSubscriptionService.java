package com.moonassist.service.email;

import com.google.common.base.Preconditions;
import com.moonassist.mailerlite.MailerLiteClient;
import com.moonassist.mailerlite.bind.Subscriber;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class EmailSubscriptionService {

  private MailerLiteClient client = new MailerLiteClient();

  private static String NEW_APP_SIGNUP_GROUP = "10702664";

  private static String IGNORE_STRING = "test@moonassist.com";

  public void addSubscription(final String name, final String email) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(email), "email can not be empty");
    if (email.contains(IGNORE_STRING)) {
      return;
    }

    Subscriber subscriber = Subscriber.builder()
        .name(name)
        .email(email)
        .build();

    client.addSubscribers(subscriber);
    client.addSubscriberToGroup(subscriber, NEW_APP_SIGNUP_GROUP);
  }

}
