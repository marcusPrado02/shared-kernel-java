package com.marcusprado02.sharedkernel.events.integration;


import java.time.Instant;
import java.util.Map;

/** Contrato público entre serviços (compatibilidade forward/backward). */
public interface IntegrationEvent {
    String eventType();        // "order.created.v1"
    int schemaVersion();       // 1, 2, 3...
    Instant occurredAt();      // UTC
    String aggregateId();      // business id (ex.: orderId)
    Map<String, Object> payload(); // dados serializáveis
}
