package com.moonassist.type;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class IdTest {

  @Test
  public void testEquality() {

    Id<AccountId> id = new Id();
    Assert.assertEquals(id, id);

    UUID uuuId = UUID.randomUUID();
    Assert.assertEquals(new Id(uuuId), new Id(uuuId));

    String someValue = uuuId.toString();
    Assert.assertEquals(Id.from(someValue), Id.from(someValue));

    Assert.assertEquals(new Id(someValue), Id.from(someValue));
  }
}
