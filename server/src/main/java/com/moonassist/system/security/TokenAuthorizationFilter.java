package com.moonassist.system.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.util.WebUtils;

import com.google.common.collect.ImmutableList;
import com.moonassist.service.DomainService;
import com.moonassist.service.authentication.AuthenticationToken;
import com.moonassist.service.authentication.TokenService;

/**
 *
 * Information from https://auth0.com/blog/implementing-jwt-authentication-on-spring-boot/
 *
 */
public class TokenAuthorizationFilter extends BasicAuthenticationFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenAuthorizationFilter.class);

  private TokenService tokenService;

  private DomainService domainService;

  public TokenAuthorizationFilter(AuthenticationManager authenticationManager, TokenService tokenService, DomainService domainService) {
    super(authenticationManager);
    this.tokenService = tokenService;
    this.domainService = domainService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {

    Cookie cookie = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME);

    String requestPath = httpServletRequest.getContextPath();


    //Fail fast
    if (cookie == null && requestPath.equals("/health")) {
      LOGGER.info("Request with no validation was made");
      chain.doFilter(httpServletRequest, httpServletResponse);
      return;
    }

    //Validate by Token in Cookie
    if (cookie != null && validateCookie(httpServletRequest)) {
      SecurityContextHolder.getContext().setAuthentication(getAuthentication(cookie));
      String refreshedCookie = tokenService.refresh(cookie.getValue());
      httpServletResponse.addCookie(SecurityConstants.createCookie(refreshedCookie, domainService.getBackendDomain()));
    }

    chain.doFilter(httpServletRequest, httpServletResponse);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(Cookie cookie) {
    try {

      AuthenticationToken authenticationToken = tokenService.decrypt(cookie.getValue());
      return new UsernamePasswordAuthenticationToken(authenticationToken, null, ImmutableList.of(new SimpleGrantedAuthority("USER_ROLE")));

    } catch (Exception e) {
      LOGGER.error("Error while processing a valid token!", e);
      return null;
    }
  }

  private boolean validateCookie(HttpServletRequest httpServletRequest) {

    Cookie cookie = WebUtils.getCookie(httpServletRequest, SecurityConstants.AUTHORIZATION_COOKIE_NAME);

    return tokenService.validate(cookie.getValue());
  }

}