package com.moonassist.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DomainService {

  @Value("${backend.domain:#{'localhost'}}")
  private String backendDomain;

  @Value("${app.domain:#{'localhost'}}")
  private String appDomain;

  public String getBackendDomain() {
    return backendDomain;
  }

  public String getAppDomain() {
    return appDomain;
  }

}
