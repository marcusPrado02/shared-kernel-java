package com.marcusprado02.sharedkernel.cqrs.command.spi;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

public final class JpaOutboxWriter implements OutboxWriter {
    private final EntityManager em;
    public JpaOutboxWriter(EntityManager em){ this.em = em; }

    @Override
    public void append(String category, String key, Map<String, Object> payload, String tenantId, String traceparent, String correlationId, String causationId) {
        var rec = new OutboxEntity();
        rec.id = java.util.UUID.randomUUID().toString().replace("-","");
        rec.category = category;
        rec.key = key;
        rec.payloadJson = toJson(payload);
        rec.tenantId = tenantId;
        rec.traceparent = traceparent;
        rec.correlationId = correlationId;
        rec.causationId = causationId;
        rec.status = "NEW";
        rec.occurredAt = Instant.now();
        em.persist(rec);
    }

    private static String toJson(Map<String,Object> m){
        try {
            var w = new com.fasterxml.jackson.databind.ObjectMapper().writer();
            return w.writeValueAsString(m);
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @Entity @Table(name="command_outbox", indexes = {
        @Index(name="idx_cmd_outbox_status", columnList = "status"),
        @Index(name="idx_cmd_outbox_category", columnList = "category")
    })
    public static class OutboxEntity {
        @Id @Column(length=32) public String id;
        @Column(nullable=false) public String category;
        @Column(nullable=false) public String key;
        @Lob @Column(nullable=false) public String payloadJson;
        @Column(nullable=true)  public String tenantId;
        @Column(nullable=true)  public String traceparent;
        @Column(nullable=true)  public String correlationId;
        @Column(nullable=true)  public String causationId;
        @Column(nullable=false) public String status;      // NEW,SENT,FAILED
        @Column(nullable=false) public Instant occurredAt;
        @Version public long version;
    }
}


