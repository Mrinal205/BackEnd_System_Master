package com.moonassist.service.email;

import com.google.common.collect.ImmutableList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmailService {

  private static final String DOMAIN = "moonassist.com";
  private static final String BASE_URI = "https://api.mailgun.net/v3/" + DOMAIN;
  private static final String API_KEY = "key-cc8de54db630a080879f09203304b336"; //TODO move this to config

  public void dispatchEmail(String subject, String to, String from, String body) throws UnirestException {

    List<NameValuePair> params = ImmutableList.of(
        new BasicNameValuePair("from", from),
        new BasicNameValuePair("to", to),
        new BasicNameValuePair("subject", subject),
        new BasicNameValuePair("html", body)
    );

    String value = URLEncodedUtils.format(params, "UTF-8");

    log.info("Dispatching email to : " + to);

    HttpResponse<JsonNode> jsonResponse = Unirest.post(BASE_URI + "/messages")
        .basicAuth("api", API_KEY)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .body( value )
        .asJson();

    if (jsonResponse.getStatus() > 202) {
      throw new RuntimeException("Dispatching email failed: " + jsonResponse.getStatus());
    }

  }

}
