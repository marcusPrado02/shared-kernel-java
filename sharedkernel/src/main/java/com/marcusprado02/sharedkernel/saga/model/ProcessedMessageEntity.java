package com.marcusprado02.sharedkernel.saga.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity @Table(name="processed_messages", uniqueConstraints=@UniqueConstraint(columnNames={"messageId","consumer"}))
public class ProcessedMessageEntity {
  @Id @GeneratedValue Long id;
  public String messageId;
  public String consumer;
  public OffsetDateTime processedAt;
}
