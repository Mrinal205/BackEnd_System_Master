package com.moonassist.persistence.user;

import com.moonassist.type.Id;
import com.moonassist.type.TwoFactorId;
import com.moonassist.type.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwofactorRepository extends JpaRepository<TwoFactorDTO, Id<TwoFactorId>> {

  TwoFactorDTO findOneByUserId(Id<UserId> userId);

}