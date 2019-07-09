package com.moonassist.bind.account;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

  public String line1;
  public String line2;
  public String city;
  public String province;
  public String postal;
  public String country;

  public void validate() {

    Preconditions.checkArgument(StringUtils.isNotEmpty(line1), "line1 can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(line2), "line2 can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(city), "city can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(province), "province can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(postal), "postal can not be empty");
    Preconditions.checkArgument(StringUtils.isNotEmpty(country), "country can not be empty");
    Preconditions.checkArgument(country.length() == 3, "country must be three characters");

  }

}
