package com.moonassist.type;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

//Todo : Support Jackson JSON parsing
@Embeddable
public class Id<T> extends Object implements Serializable {

  private UUID id;

  public Id() {
    id = UUID.randomUUID();
  }

  public Id(UUID uuid) {
    this.id = uuid;
  }

  public Id(String uuid) {
    Preconditions.checkArgument(StringUtils.isNotEmpty(uuid), "uuid is empty");
    id = UUID.fromString(uuid);
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object id) {

    if (this.id == id) {
      return true;
    }

    if (! (id instanceof Id)) {
      return false;
    }

    //TODO verify generic class is same

    return id.equals( ((Id) id).internalValue() );
  }

  public UUID internalValue() {
    return id;
  }

  public String value() {
    return id.toString();
  }

  public static final Id from(String input) {
    return new Id(input);
  }

}