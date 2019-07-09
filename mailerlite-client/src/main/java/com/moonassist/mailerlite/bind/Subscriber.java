package com.moonassist.mailerlite.bind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subscriber {

  private String id;
  private String name;
  private String email;
  private String type;

  @JsonProperty("country_id")
  private Integer countryId;
  private Integer sent;

  private List<Field> fields;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonProperty("date_created")
  private Date dateCreated;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonProperty("date_updated")
  private Date dateUpdated;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonProperty("date_subscribe")
  private Date dateSubscribe;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonProperty("date_unsubscribe")
  private Date dateUnsubscribe;

}
