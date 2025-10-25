package com.marcusprado02.sharedkernel.events.domain;

import java.time.Instant;

public interface DomainEvent {
    /** Nome estável no domínio (não acoplar a classe). */
    String eventName();
    /** Versão do schema tático do domínio (ex.: 1). */
    default int version() { return 1; }

    /** Timestamp do evento (geralmente na criação). */
    Instant occurredOn();

    /** ID único do evento (geralmente na criação). */
    String id();

    String source();  // ex.: "order-service"

    String correlationId(); // para rastreamento (ex.: traceId)

    /** ID da entidade agregada relacionada (se aplicável). */
    String aggregateId();

    /** Tipo da entidade agregada relacionada (se aplicável). */
    String aggregateType();

    /** Dados do evento (payload). */
    Object data();

    /** Tipo do evento (classe). */
    default String type() { return getClass().getName(); }

    /** Versão do schema técnico (para upcasting, se necessário). */
    default int schemaVersion() { return 1; }

    /** Categoria do evento (ex.: "order", "payment"). */
    default String category() {
        var at = aggregateType();
        if (at == null || at.isBlank()) return "unknown";
        return at.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    }   

    /** Tópico Kafka sugerido (ex.: "order.v1"). */
    default String topic() {
        return category() + ".v" + version();
    }
    
}
