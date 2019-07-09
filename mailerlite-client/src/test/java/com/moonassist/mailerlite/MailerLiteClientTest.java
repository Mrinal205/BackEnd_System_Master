package com.moonassist.mailerlite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.moonassist.mailerlite.bind.Field;
import com.moonassist.mailerlite.bind.Group;
import com.moonassist.mailerlite.bind.Subscriber;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class MailerLiteClientTest {

  private MailerLiteClient client = new MailerLiteClient();

  @Test
  public void testCallAuthenticate() {

    CloseableHttpResponse response = client.authenticate();

    Assert.assertNotNull(response);
  }

  @SneakyThrows
  @Test
  public void testCallGetSubscribers() {

    List<Subscriber> response = client.getSubscribers();

    Assert.assertNotNull(response);
    Assert.assertFalse(response.isEmpty());

//    response.stream().forEach( subscriber -> {
//      System.out.println(subscriber.getName() + " " + subscriber.getEmail());
//    });

  }

  @SneakyThrows
  @Test
  public void testCallGetGroups() {

    List<Group> response = client.getGroups();

    Assert.assertNotNull(response);
    Assert.assertFalse(response.isEmpty());

    response.stream().forEach( group -> {
      System.out.println(group.getId() + " " + group.getName());
    });

  }

  @Ignore
  @Test
  public void testCallAddSubscriberToGroup() {

   //10702664 "New app signup"

    Subscriber subscriber = Subscriber.builder()

        .email("AndersonAnderson+testsub@gmail.com")
        .name("Test Subscriber")
        .fields(ImmutableList.of(Field.builder()
            .key("company")
            .value("MoonAssist")
            .type("TEXT")
            .build()))
        .build();

    Subscriber subscriberResponse = client.addSubscribers(subscriber);
    Assert.assertNotNull(subscriberResponse);

    subscriberResponse = client.addSubscriberToGroup(subscriber, "10702664");
    System.out.println(subscriberResponse);

  }

}
