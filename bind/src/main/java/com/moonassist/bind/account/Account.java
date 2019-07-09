package com.moonassist.bind.account;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.moonassist.bind.authenticate.Authenticated;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Account extends Authenticated {

  @Builder
  public Account(String token, String id, Address address, Set<Exchange> exchanges, List<LoginEvent> loginEvents,
                 Boolean twoFactorEnabled, PersonalDetails personalDetails, Subscription subscription,
                 List<String> whiteListedIpAddress, Set<EmailSubscription> emailSubscriptions) {
    super(token);
    this.id = id;
    this.address = address;
    this.exchanges = exchanges;
    this.loginEvents = loginEvents;
    this.twoFactorEnabled = twoFactorEnabled;
    this.personalDetails = personalDetails;
    this.subscription = subscription;
    this.whiteListedIpAddress = whiteListedIpAddress;
    this.emailSubscriptions = emailSubscriptions;
  }

  public String id;
  public PersonalDetails personalDetails;
  public Address address;
  public Set<Exchange> exchanges;
  public List<LoginEvent> loginEvents;
  public Boolean twoFactorEnabled;
  public Subscription subscription;
  public List<String> whiteListedIpAddress;
  public Set<EmailSubscription> emailSubscriptions;

  public void validate() {
    Preconditions.checkArgument(personalDetails != null, "PersonalDetails is required");
    Preconditions.checkArgument(address != null, "address object is required");
  }

}
