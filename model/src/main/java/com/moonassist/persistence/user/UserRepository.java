package com.moonassist.persistence.user;

import java.util.Set;

import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserDTO, Id<UserId>> {

  UserDTO findOneByEmail(String email);

  Set<UserDTO> findAllByStatus(UserStatus userStatus);

}