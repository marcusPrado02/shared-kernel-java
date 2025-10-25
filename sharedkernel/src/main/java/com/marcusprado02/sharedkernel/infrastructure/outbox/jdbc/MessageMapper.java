package com.marcusprado02.sharedkernel.infrastructure.outbox.jdbc;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxRecord;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageHeaders;

final class MessageMapper {

  static <T> MessageEnvelope<T> fromEnvelope(OutboxRecord rec,
                                             MessageEnvelope<T> in,
                                             String contentType) {
    String id    = notBlankOr(in.messageId(), rec.id());
    String topic = notBlankOr(in.topic(),     rec.topic());
    String key   = notBlankOr(in.key(),       rec.key());

    MessageHeaders hdr = ensureHeaders(in.headers(), contentType);

    return new MessageEnvelope<>(id, topic, key, in.payload(), hdr,
        in.occurredAt() != null ? in.occurredAt() : Instant.now());
  }

  @SuppressWarnings("unchecked")
  static <T> MessageEnvelope<T> fromCloudEventsMap(OutboxRecord rec,
                                                   Map<String,Object> ce,
                                                   String contentType) {
    String id     = asString(ce.getOrDefault("id", rec.id()));
    String topic  = asString(ce.getOrDefault("type", rec.topic()));
    String subject= asString(ce.getOrDefault("subject", rec.key()));

    Object dataObj = ce.getOrDefault("data", Map.of());
    T data = (T) dataObj;

    Map<String,Object> ext = asMap(ce.getOrDefault("extensions", Map.of()));

    String correlationId = asString(ext.get("correlationId"));
    String causationId   = asString(ext.get("causationId"));
    String tenantId      = asString(ext.get("tenantId"));
    String contentTypeCE = asString(ce.get("datacontenttype"));
    if (contentTypeCE != null && !contentTypeCE.isBlank()) contentType = contentTypeCE;

    String userId = asString(ext.get("userId"));
    String traceId = asString(ext.get("traceId"));

    MessageHeaders headers = new MessageHeaders(
        correlationId,
        causationId,
        tenantId,
        userId,
        contentType,
        Map.of(),
        contentType,
        contentTypeCE,
        traceId
    );

    return new MessageEnvelope<>(id, topic, subject, data, headers, Instant.now());
  }

  // --------- helpers

  private static String notBlankOr(String v, String fallback) {
    return (v == null || v.isBlank()) ? fallback : v;
    }

  private static String asString(Object v) { return v == null ? null : Objects.toString(v, null); }

  @SuppressWarnings("unchecked")
  private static Map<String,Object> asMap(Object v) {
    if (v == null) return Map.of();
    if (v instanceof Map<?,?> m) return (Map<String,Object>) m;
    throw new IllegalArgumentException("Esperado Map<String,Object>, veio " + v.getClass());
  }

  private static MessageHeaders ensureHeaders(MessageHeaders h, String contentType) {
    if (h == null || h.contentType() == null || h.contentType().isBlank()) {
      if (h == null) return new MessageHeaders(null, null, null, null, contentType, Map.of(), contentType, contentType, contentType);
      return new MessageHeaders(h.correlationId(), h.causationId(), h.tenantId(), h.traceId(), contentType, h.kv(), contentType, contentType, contentType);
    }
    return h;
  }

  private MessageMapper() {}
}
