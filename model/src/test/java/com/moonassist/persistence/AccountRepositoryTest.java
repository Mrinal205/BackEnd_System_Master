package com.moonassist.persistence;

import com.moonassist.persistence.account.AccountDTO;
import com.moonassist.persistence.account.AccountRepository;
import com.moonassist.persistence.user.UserDTO;
import com.moonassist.persistence.user.UserRepository;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.util.Date;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {H2Config.class})
@Transactional
public class AccountRepositoryTest {

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private UserRepository userRepository;

  @Test
  public void testFindOne() {

    Id<UserId> userId = new Id<>();
    Id<AccountId> accountId = new Id<>();
    Date now = new Date();

    UserDTO userDTO = userRepository.save(UserDTO.builder()
        .id(userId)
        .name("Test User")
        .build());

    AccountDTO accountDTO = accountRepository.save(
        AccountDTO.builder()
            .id(accountId)
            .user(userDTO)
            .phone("801-876-5309")
            .created(now)
            .updated(now)
            .build()
    );

    Assert.assertNotNull("Failed to persist account", accountDTO);

    AccountDTO result = accountRepository.findOne(accountId);

    Assert.assertNotNull("Failed to find account", result);
  }

}
