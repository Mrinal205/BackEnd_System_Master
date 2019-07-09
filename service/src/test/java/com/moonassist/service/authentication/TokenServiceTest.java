package com.moonassist.service.authentication;

import com.moonassist.persistence.user.UserDTO;
import com.moonassist.service.EncryptionService;
import com.moonassist.service.TimeService;
import com.moonassist.service.UserService;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {EncryptionService.class, TokenService.class})
@TestPropertySource(properties = {
    "encryption.key=Bar12345Bar12345",
})
public class TokenServiceTest {

  @MockBean
  private TimeService mockTimeService;

  @MockBean
  private UserService userService;

  @Autowired
  private TokenService tokenService;

  @Test
  public void testCreateToken() {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());

    Mockito.when(mockTimeService.now()).thenReturn(new Date());
    Mockito.when(userService.find(Mockito.eq(userId))).thenReturn(UserDTO.builder().build());

    String result = tokenService.createToken(userId, "someemail@asdf.com");

    Assert.assertNotNull(result);
  }

  @Test
  public void testValidate() {

    Id<UserId> userId = Id.from(UUID.randomUUID().toString());

    UserDTO userDTO = UserDTO.builder()
        .id(userId)
        .passwordHash("asdfasdfasdfgsdfg")
        .build();


    Mockito.when(userService.find(Mockito.eq(userId))).thenReturn(userDTO);
    Mockito.when(mockTimeService.now()).thenReturn(new Date());

    String result = tokenService.createToken(new Id(), "someemail@asdf.com");

    Assert.assertTrue(tokenService.validate(result));

  }

  @Test
  public void testValidate_expired() {
    Id<UserId> userId = Id.from(UUID.randomUUID().toString());

    UserDTO userDTO = UserDTO.builder()
        .id(userId)
        .passwordHash("asdfasdfasdfgsdfg")
        .build();

    Mockito.when(userService.find(Mockito.eq(userId))).thenReturn(userDTO);
    Mockito.when(mockTimeService.now()).thenReturn(new Date());
    String result = tokenService.createToken(new Id(), "someemail@asdf.com");

    Mockito.when(mockTimeService.now()).thenReturn(DateUtils.addMinutes(new Date(), 60));
    Assert.assertFalse(tokenService.validate(result));
  }

  @Test
  public void testPasswordChangeInvalidates() {
    Id<UserId> userId = Id.from(UUID.randomUUID().toString());

    UserDTO userDTO = UserDTO.builder()
            .id(userId)
            .passwordHash("asdfasdfasdfgsdfg")
            .build();

    Mockito.when(userService.find(Mockito.eq(userId))).thenReturn(userDTO);
    Mockito.when(mockTimeService.now()).thenReturn(new Date());
    String result = tokenService.createToken(new Id(), "someemail@asdf.com");

    userDTO.setPasswordHash("aasdg23j4kjhdfkjhsdf");

    String resultAfter = tokenService.createToken(new Id(), "someemail@asdf.com");

    Mockito.when(mockTimeService.now()).thenReturn(DateUtils.addMinutes(new Date(), 60));
    Assert.assertFalse(result == resultAfter);
  }

}
