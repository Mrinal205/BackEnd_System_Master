package com.moonassist.persistence.user;

import com.moonassist.type.EventId;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventDTO, Id<EventId>> {

  //TODO add index on DB
  List<EventDTO> findAllByUserIdAndEventTypeOrderByCreatedDesc(Id<UserId> userId, EventType eventType, Pageable pageable);

}