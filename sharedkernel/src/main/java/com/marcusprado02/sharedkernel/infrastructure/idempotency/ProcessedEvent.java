package com.marcusprado02.sharedkernel.infrastructure.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity @Table(name="projection_processed_events",
    indexes=@Index(name="idx_proj_event_unique", columnList="projectionName,eventId", unique=true))
public class ProcessedEvent {
    @Id @GeneratedValue private Long id;
    private String projectionName;
    private String eventId;
    private long sequence;
    private java.time.OffsetDateTime processedAt;
    // getters/setters
}
