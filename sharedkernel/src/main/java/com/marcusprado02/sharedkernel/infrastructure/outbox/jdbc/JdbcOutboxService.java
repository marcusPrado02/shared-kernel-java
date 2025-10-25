package com.marcusprado02.sharedkernel.infrastructure.outbox.jdbc;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxService;

import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.*;

public class JdbcOutboxService implements OutboxService {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;
  private final ObjectMapper om;

  public JdbcOutboxService(JdbcTemplate jdbc, TransactionTemplate tx, ObjectMapper om) {
    this.jdbc = jdbc; this.tx = tx; this.om = om;
  }

  @Override public Mono<Void> enqueue(String topic, String key, Object payload, Map<String,String> headers) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(status -> {
      String id = Ulids.next(); // ULID/UUID
      String contentType = "application/cloudevents+json";
      String shard = Shards.key(topic, key);
      String json = toEnvelopeJson(id, topic, key, payload, headers, contentType);

      jdbc.update("""
        INSERT INTO outbox (id, topic, msg_key, payload, content_type, headers, created_at, status, attempt, next_attempt_at, shard_key)
        VALUES (?,?,?,?,?,?::jsonb, ?, 'PENDING', 0, now(), ?)
      """, id, topic, key, json, contentType, toJson(headers), java.sql.Timestamp.from(Instant.now()), shard);
    }));
  }

  private String toEnvelopeJson(String id, String topic, String key, Object payload, Map<String,String> headers, String ct) {
    try {
      // CloudEvents minimal no data: data = payload serializado
      var envelope = Map.of(
        "id", id,
        "type", topic,
        "source", "urn:outbox:"+topic,
        "subject", key,
        "time", Instant.now().toString(),
        "data", payload,
        "datacontenttype", ct,
        "extensions", headers==null? Map.of(): headers
      );
      return om.writeValueAsString(envelope);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  private String toJson(Object o){ try { return om.writeValueAsString(o);} catch(Exception e){ throw new RuntimeException(e);} }

  static final class Ulids { static String next(){ return java.util.UUID.randomUUID().toString(); } }
  static final class Shards {
    static String key(String topic, String key) {
      int n = Integer.getInteger("OUTBOX_SHARDS", 32);
      int h = java.util.Objects.hash(topic, key);
      int b = Math.abs(h % n);
      return String.format("%s-%02d", topic, b);
    }
  }
}
