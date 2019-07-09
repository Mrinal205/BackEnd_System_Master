package com.moonassist.persistence.account;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.moonassist.type.AccountBalanceId;
import com.moonassist.type.AccountId;
import com.moonassist.type.Id;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface NetWorthRepository extends CrudRepository<NetWorthDTO, Id<AccountBalanceId>> {

  List<NetWorthDTO> findAllByAccountId(final Id<AccountId> accountId, Pageable pageable);

}
