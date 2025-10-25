package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.Map;

/** Metadados padrão (correlation/causation/tenant/security/etc.). */
public record EventMetadata(
    String correlationId,
    String causationId,
    String tenantId,
    String userId,
    String source,                 // microserviço/frota
    String ip,
    String schema,                 // "cloudevents+json", "avro", etc.
    Map<String, String> kv         // extras
) {
  public static EventMetadata minimal() { return new EventMetadata(null,null,null,null,null,null,null, Map.of()); }
}