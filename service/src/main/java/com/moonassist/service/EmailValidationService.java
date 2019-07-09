package com.moonassist.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;

import lombok.SneakyThrows;

@Service
public class EmailValidationService {


  private static final String RESTRICTED_FILE = "/restricted-email.txt";

  private final Set<String> restrictedEmails;

  @SneakyThrows
  public EmailValidationService() {

    InputStream in = getClass().getResourceAsStream(RESTRICTED_FILE);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    String rawImput = IOUtils.toString(reader);
    restrictedEmails = Splitter.on("\n").splitToList(rawImput).stream()
         .map( EmailValidationService::filterEmail )
         .collect(Collectors.toSet());
  }

  private static final String MOON_ASSIST_DOMAIN = "@moonassist.com";

  public boolean isApproved(final String email) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(email), "email can not be empty");

    return true;

//    if (email.endsWith(MOON_ASSIST_DOMAIN)) {
//      return true;
//    }
//
//    return restrictedEmails.contains( filterEmail(email) );
  }

  public static String filterEmail(final String email) {

    Preconditions.checkArgument(StringUtils.isNotEmpty(email), "email can not be empty");

    return email
//        .replaceFirst("", " ") //Remove anything between + and @ as that is a alias
        .toLowerCase();

  }

}
