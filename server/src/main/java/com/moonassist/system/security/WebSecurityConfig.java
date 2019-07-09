package com.moonassist.system.security;

import com.moonassist.service.DomainService;
import com.moonassist.service.authentication.TokenService;
import com.moonassist.service.exchange.connection.ExchangeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private TokenService tokenService;

  @Autowired
  private DomainService domainService;

  @Autowired
  private ExchangeService exchangeService;

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http
      .cors().and()
      .csrf().disable().authorizeRequests()
      .antMatchers(HttpMethod.POST, "/users").permitAll()
      .antMatchers(HttpMethod.POST, "/users/authenticate").permitAll()
      .antMatchers(HttpMethod.DELETE, "/users/authenticate").permitAll()
      .antMatchers(HttpMethod.POST, "/users/validate").permitAll()
      .antMatchers(HttpMethod.POST, "/users/forgotpassword").permitAll()
      .antMatchers(HttpMethod.POST, "/users/forgotpassword/verify").permitAll()
      .antMatchers(HttpMethod.POST, "/2fa/authenticate").permitAll()
      .antMatchers(HttpMethod.GET, "/health").permitAll()
      .anyRequest().authenticated()
      .and()
      .addFilter(new TokenAuthorizationFilter(authenticationManager(), tokenService, domainService))
      .addFilter(new RequestInitiationFilter(authenticationManager(), exchangeService))
      // this disables session creation on Spring Security
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowCredentials(true);
    configuration.setAllowedOrigins(Arrays.asList("*"));
    configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE"));
    configuration.setAllowedHeaders(Arrays.asList("content-type"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

}