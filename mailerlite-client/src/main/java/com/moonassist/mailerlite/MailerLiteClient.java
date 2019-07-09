package com.moonassist.mailerlite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moonassist.mailerlite.bind.Group;
import com.moonassist.mailerlite.bind.Subscriber;
import com.moonassist.mailerlite.response.GroupsResponseHandler;
import com.moonassist.mailerlite.response.SubscriberResponseHandler;
import com.moonassist.mailerlite.response.SubscribersResponseHandler;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;

public class MailerLiteClient {

  private static final String API_KEY = "acb413039794660f814495ffdc25f4c1";

  private static final String API = "https://api.mailerlite.com/api/v2";

  private ObjectMapper objectMapper = new ObjectMapper();

  /**
   * curl -v https://api.mailerlite.com/api/v2 \
   * -H "X-MailerLite-ApiKey: {replace-it-with-your-api-key}"
   * -H "Content-Type: application/json"
   */

  @SneakyThrows
  public CloseableHttpResponse authenticate() {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(API);
    httpGet.setHeader("X-MailerLite-ApiKey", API_KEY);
    httpGet.setHeader("Content-Type", "application/json");

    return httpclient.execute(httpGet);

  }

  @SneakyThrows
  public List<Subscriber> getSubscribers() {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(API + "/subscribers");
    httpGet.setHeader("X-MailerLite-ApiKey", API_KEY);
    httpGet.setHeader("Content-Type", "application/json");

    return httpclient.execute(httpGet, new SubscribersResponseHandler());
  }

  @SneakyThrows
  public Subscriber addSubscribers(final Subscriber subscriber) {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(API + "/subscribers");
    httpPost.setHeader("X-MailerLite-ApiKey", API_KEY);
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(subscriber)));

    return httpclient.execute(httpPost, new SubscriberResponseHandler());
  }

  @SneakyThrows
  public List<Group> getGroups() {
    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpGet httpGet = new HttpGet(API + "/groups");
    httpGet.setHeader("X-MailerLite-ApiKey", API_KEY);
    httpGet.setHeader("Content-Type", "application/json");

    return httpclient.execute(httpGet, new GroupsResponseHandler());
  }

  @SneakyThrows
  public Subscriber addSubscriberToGroup(final Subscriber subscriber, final String groupId) {

    CloseableHttpClient httpclient = HttpClients.createDefault();
    HttpPost httpPost = new HttpPost(API + "/groups/" + groupId + "/subscribers");
    httpPost.setHeader("X-MailerLite-ApiKey", API_KEY);
    httpPost.setHeader("Content-Type", "application/json");
    httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(subscriber)));

    return httpclient.execute(httpPost, new SubscriberResponseHandler());
  }

}
