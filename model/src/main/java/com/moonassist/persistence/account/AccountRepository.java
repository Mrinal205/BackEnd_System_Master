package com.moonassist.persistence.account;

import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<AccountDTO, Id<AccountId>> {

}