package com.marcusprado02.sharedkernel.infrastructure.messaging;

import java.util.Map;

/** Cabeçalhos padrão e extensíveis. */
public record MessageHeaders(
    String correlationId,
    String causationId,
    String tenantId,
    String userId,
    String schema,               // "cloudevents+json" | "avro" | "proto"
    Map<String, String> kv,
    String contentType,          // "application/json" | "application/cloudevents+json" | "application/avro" | "application/proto"
    String originalContentType,   // se veio via HTTP, o Content-Type original (pode ser diferente do contentType acima)
    String traceId

) {
  public static MessageHeaders minimal() { return new MessageHeaders(null,null,null,null,"application/json", Map.of(), "application/json", null, null); }
}
