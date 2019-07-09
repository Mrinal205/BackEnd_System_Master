package com.moonassist.service;

import com.google.common.base.Preconditions;
import com.moonassist.persistence.user.*;
import com.moonassist.type.Id;
import com.moonassist.type.UserId;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EventsService {

  @Autowired
  private EventRepository eventRepository;

  @Autowired
  private UserRepository userRepository;

  public void login(Id<UserId> userId, String ipAddress, String userAgent) {
    Preconditions.checkArgument(userId != null, "UserId is required");
    Preconditions.checkArgument(StringUtils.isNotEmpty(ipAddress), "ip address is required");

    UserDTO userDTO = userRepository.findOne(userId);
    Preconditions.checkArgument(userDTO != null, "Could not find user by id: " + userId);

    EventDTO eventDTO = EventDTO.builder()
        .id(new Id<>())
        .eventType(EventType.LOGIN)
        .ipAddress(ipAddress)
        .user(userDTO)
        .userAgent(userAgent)
        .created(new Date())
        .build();

    eventRepository.save(eventDTO);
  }

  /**
   * Finds the all events for the given params
   * @param userId
   * @param eventType
   */
  public List<EventDTO> all(Id<UserId> userId, EventType eventType) {

    Preconditions.checkArgument(userId != null, "userId can not be empty");
    Preconditions.checkArgument(eventType != null, "EventType can not be absent");

    Pageable topTwenty = new PageRequest(0, 20);

    return eventRepository.findAllByUserIdAndEventTypeOrderByCreatedDesc(userId, eventType, topTwenty);
  }

}