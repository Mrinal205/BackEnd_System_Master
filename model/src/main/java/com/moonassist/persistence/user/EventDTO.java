package com.moonassist.persistence.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.moonassist.type.EventId;
import com.moonassist.type.Id;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class EventDTO {

  @Embedded
  @javax.persistence.Id
  @Column(name = "id")
  private Id<EventId> id;

  @Column(nullable = false, updatable = false)
  private Date created;

  @Column(updatable = false)
  private String details;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, updatable = false)
  private EventType eventType;

  @Column(name = "ip_address", nullable = false, updatable = false)
  private String ipAddress;

  @Column(name = "user_agent", updatable = false)
  private String userAgent;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private UserDTO user;

}
