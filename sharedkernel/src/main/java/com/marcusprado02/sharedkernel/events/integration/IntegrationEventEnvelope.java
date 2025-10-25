package com.marcusprado02.sharedkernel.events.integration;

import java.util.Map;
import java.time.Instant;

public record IntegrationEventEnvelope(
    String id,
    String correlationId,
    String causationId,
    String tenantId,
    String traceId,
    Object payload,                 // ou "event"
    Map<String,String> headers
) {
    public static IntegrationEventEnvelope fromLegacy(String eventType, String payloadJson, Map<String,String> headers) {
        var id   = headers.getOrDefault("id", "");
        var corr = headers.get("corr");
        var caus = headers.get("caus");
        var ten  = headers.get("tenant");
        var trc  = headers.get("trace");
        return new IntegrationEventEnvelope(id, corr, caus, ten, trc, new Raw(eventType, payloadJson), headers);
    }
    /** Payload bruto (type + json) para compat/telemetria. */
    public static final class Raw {
        public final String type;
        public final String json;
        public Raw(String type, String json) { this.type = type; this.json = json; }
    }

    
}
