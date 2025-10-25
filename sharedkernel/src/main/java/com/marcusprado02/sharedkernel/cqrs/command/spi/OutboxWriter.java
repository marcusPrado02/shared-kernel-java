package com.marcusprado02.sharedkernel.cqrs.command.spi;

import java.util.Map;

/**
 * Porta de escrita para Outbox (CDC/auditoria).
 * “category” pode ser “command.{Name}.completed” etc.
 */
public interface OutboxWriter {
    void append(String category,
                String key,
                Map<String, Object> payload,
                String tenantId,
                String traceparent,
                String correlationId,
                String causationId);
}