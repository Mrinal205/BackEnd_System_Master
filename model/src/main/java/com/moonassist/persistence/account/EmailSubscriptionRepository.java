package com.moonassist.persistence.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.moonassist.type.EmailSubscriptionId;
import com.moonassist.type.Id;

@Repository
public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscriptionDTO, Id<EmailSubscriptionId>> {

}