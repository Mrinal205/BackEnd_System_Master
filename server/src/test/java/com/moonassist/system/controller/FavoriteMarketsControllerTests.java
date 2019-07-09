package com.moonassist.system.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.moonassist.IntegrationTest;
import com.moonassist.TestingConstants;
import com.moonassist.bind.account.Exchange;
import com.moonassist.bind.account.ExchangeEnum;
import com.moonassist.bind.account.FavoriteMarket;
import com.moonassist.bind.authenticate.AuthenticateResponse;
import com.moonassist.config.H2Config;
import com.moonassist.config.TestConfig;
import com.moonassist.system.security.WebSecurityConfig;
import com.moonassist.util.TestUserHelper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {H2Config.class, TestConfig.class, WebSecurityConfig.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@ComponentScan
public class FavoriteMarketsControllerTests extends IntegrationTest {

    @LocalServerPort
    private int port;
    private TestRestTemplate testRestTemplate = new TestRestTemplate();
    private String host;

    @Autowired
    private TestUserHelper testUserHelper;

    @Before
    public void setUp() {
        host = "http://localhost:" + port;
    }

    @Test
    public void addFavoriteMarket() throws Exception {
        ResponseEntity<AuthenticateResponse> response = testUserHelper.createUser(host);

        // Add Exchange
        Exchange exchange = Exchange.builder().apiKey(TestingConstants.BINANCE_API_KEY).secret(TestingConstants.BINANCE_SECRET_KEY)
                .exchangeName(ExchangeEnum.BINANCE).build();

        HttpEntity<Exchange> httpEntity = request(exchange, token(response));
        ResponseEntity<Exchange> exchangeResponse = testRestTemplate.exchange(host + "/accounts/" + response.getBody().getAccountId() + "/exchanges",
                HttpMethod.POST, httpEntity, Exchange.class);
        Assert.assertEquals(HttpStatus.OK, exchangeResponse.getStatusCode());
        String exchangeId = exchangeResponse.getBody().getId();

        FavoriteMarket favoriteMarket = FavoriteMarket.builder().symbolPair(RandomStringUtils.random(5)).exchangeId(exchangeId).build();

        HttpEntity<FavoriteMarket> favoriteMarketHttpEntity = request(favoriteMarket, token(response));
        ResponseEntity<FavoriteMarket> favoriteMarketResponse = testRestTemplate.exchange(host + "/favorites/markets", HttpMethod.POST,
                favoriteMarketHttpEntity, FavoriteMarket.class);
        Assert.assertEquals(HttpStatus.OK, favoriteMarketResponse.getStatusCode());
    }

}