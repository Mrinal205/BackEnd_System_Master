package com.moonassist.system.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorsFilter implements Filter {

  private static Logger LOGGER = LoggerFactory.getLogger(CorsFilter.class);

  private static String METHOD = "OPTIONS";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {



      HttpServletRequest request = (HttpServletRequest) servletRequest;

      LOGGER.info("FOUND METHOD : " + request.getMethod());


      if ( METHOD.equals(request.getMethod()) ) {

        LOGGER.info("Calling CORS filter");

        HttpServletResponse response = (HttpServletResponse) servletResponse;

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT,OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "180");

        response.setStatus(200);

        filterChain.doFilter(servletRequest, response);
      }
    }

    @Override
    public void destroy() {

    }
}