package com.moonassist.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moonassist.type.AddressId;
import com.moonassist.type.Id;

@Repository
public interface AddressRepository extends JpaRepository<AddressDTO, Id<AddressId>> {

}