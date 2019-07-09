package com.moonassist.service.authentication;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

//TODO Need to add randomized data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class AuthenticationToken {

  private String userId;
  private String email;
  private Date lastRequest;
  private String passwordHash;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getLastRequest() {
    return lastRequest;
  }

  public void setLastRequest(Date lastRequest) {
    this.lastRequest = lastRequest;
  }

  public String getPasswordHash() { return passwordHash; }
}