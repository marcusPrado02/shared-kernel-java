package com.marcusprado02.sharedkernel.events.outbox;


import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name = "outbox", indexes = {
    @Index(name="idx_outbox_status_next", columnList = "status,nextAttemptAt"),
    @Index(name="idx_outbox_type", columnList = "eventType")
})
public class OutboxRecord {
    @Id @Column(length = 32) public String id; // ULID/KSUID
    @Column(nullable=false) public String eventType;     // "order.created.v1"
    @Column(nullable=false) public Integer schemaVersion;
    @Column(nullable=false) public String aggregateId;
    @Column(nullable=false) public Instant occurredAt;
    @Column(nullable=false) public String tenantId;
    @Column(nullable=false) public String payloadJson;   // JSON estável (Avro/Proto opcional)
    @Column(nullable=false) public String status;        // NEW, SENT, FAILED
    @Column(nullable=false) public Integer attempts;
    @Column(nullable=false) public Instant nextAttemptAt;
    @Column(nullable=true)  public String errorMessage;
    @Column(nullable=true)  public String traceparent;
    @Column(nullable=true)  public String correlationId;
    @Column(nullable=true)  public String causationId;
    @Column(nullable=true)  public String key;           // particionamento (opcional)
    @Column(nullable=true)  public String topic;         // tópico/exchange (opcional
    @Column(nullable=true)  public Instant sentAt;
    @Column(nullable=true)  public Instant processedAt;
    @Column(nullable=true)  public Instant completedAt;

    @Version public long version; // optimistic locking
}
