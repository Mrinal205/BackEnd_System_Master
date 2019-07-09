package com.moonassist.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moonassist.type.ExchangeId;
import com.moonassist.type.Id;

@Repository
public interface ExchangeRepository extends JpaRepository<ExchangeDTO, Id<ExchangeId>> {

}