package com.moonassist;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Preconditions;

public class IntegrationTest{

  public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36";

  public TestRestTemplate client() {
    return new TestRestTemplate();
  }


  public HttpEntity<Void> voidRequest(String token) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(token));

    return new HttpEntity<>(httpHeaders(Optional.of(token)));
  }

  public <T> HttpEntity<T> request(T object, String token) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(token));
    Preconditions.checkArgument(object != null);

    return new HttpEntity<T>(object, httpHeaders(Optional.of(token)));
  }

  public <T> HttpEntity<T> request(T object) {
    Preconditions.checkArgument(object != null);

    return new HttpEntity<T>(object, httpHeaders(Optional.empty()));
  }

  private HttpHeaders httpHeaders(Optional<String> token) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.set("user-agent", USER_AGENT);
    if (token.isPresent()) {
      httpHeaders.add(HttpHeaders.COOKIE, "moon-auth" + "=" + token.get());
    }
    return httpHeaders;
  }

  public void assertAuthCookie(ResponseEntity<?> responseEntity){

    Assert.assertTrue(responseEntity.getHeaders().containsKey("Set-Cookie"));
    Assert.assertTrue(responseEntity.getHeaders().get("Set-Cookie").get(0).contains("moon-auth"));

  }

  public String token(ResponseEntity<?> responseEntity) {
    assertAuthCookie(responseEntity);

    String cookieValue = responseEntity.getHeaders().get("Set-Cookie").get(0);
    return cookieValue.replace("moon-auth=", "");

  }

}
