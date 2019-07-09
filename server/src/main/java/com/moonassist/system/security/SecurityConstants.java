package com.moonassist.system.security;

import javax.servlet.http.Cookie;

public class SecurityConstants {

  public static final String AUTHORIZATION_COOKIE_NAME = "moon-auth";

  public static Cookie createCookie(String token, String domain) {
    Cookie tokenCookie = new Cookie(SecurityConstants.AUTHORIZATION_COOKIE_NAME, token);
    //TODO set domain on cookie
    tokenCookie.setPath("/");
    if ( ! "localhost".equals(domain)) {
      tokenCookie.setSecure(true);
    }
    tokenCookie.setHttpOnly(true);
    tokenCookie.setDomain(domain);
    tokenCookie.setMaxAge(3600);
    return tokenCookie;
  }

  public static Cookie logoutCookie(String domain) {
    Cookie tokenCookie = new Cookie(SecurityConstants.AUTHORIZATION_COOKIE_NAME, null);
    //TODO set domain on cookie
    tokenCookie.setPath("/");
    if ( ! "localhost".equals(domain)) {
      tokenCookie.setSecure(true);
    }
    tokenCookie.setHttpOnly(true);
    tokenCookie.setDomain(domain);
    tokenCookie.setMaxAge(0); //Tells browser to delete
    return tokenCookie;
  }

}

