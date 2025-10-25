package com.marcusprado02.sharedkernel.domain.exception.model;

import java.util.Map;

/** Contexto seguro (sem PII). */
public record ErrorContext(
        String tenantId,
        String aggregateType,
        String aggregateId,
        String correlationId,
        Map<String, String> attributes // ex.: {"command":"AuthorizePayment","attempt":"2"}
) {
    public static ErrorContext minimal(String correlationId) {
        return new ErrorContext(null, null, null, correlationId, Map.of());
    }

    public boolean isClientSafe() {
        return attributes != null && "true".equalsIgnoreCase(attributes.get("clientSafe"));
    }
}