package com.moonassist.system.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.moonassist.service.exchange.connection.ExchangeService;

/**
 * Not currently used as we allow Spring Scope Request to do this work
 */
public class RequestInitiationFilter extends BasicAuthenticationFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthorizationFilter.class);

  private ExchangeService exchangeService;

  public RequestInitiationFilter(AuthenticationManager authenticationManager, ExchangeService exchangeService) {
    super(authenticationManager);
    this.exchangeService = exchangeService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {

    chain.doFilter(httpServletRequest, httpServletResponse);

  }

}
