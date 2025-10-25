package com.marcusprado02.sharedkernel.infrastructure.outbox.jdbc;

import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageProducer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageSerializer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.ProducerOptions;
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxRecord;   // <- mantenha ESTE
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxRelay;
import com.marcusprado02.sharedkernel.infrastructure.outbox.OutboxStatus;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class JdbcOutboxRelay implements OutboxRelay {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;
  private final MessageProducer producer;
  private final MessageSerializer serializer;
  private final String producerId;
  private final Duration lease = Duration.ofSeconds(30);
  private final int batchSize;
  private final String shard;
  private final int maxAttempts;
  private final Duration baseBackoff;

  public JdbcOutboxRelay(JdbcTemplate jdbc, TransactionTemplate tx, MessageProducer producer,
                         MessageSerializer serializer, String shard, int batchSize,
                         int maxAttempts, Duration baseBackoff, String producerId) {
    this.jdbc = jdbc; this.tx = tx; this.producer = producer; this.serializer = serializer;
    this.shard = shard; this.batchSize = batchSize; this.maxAttempts = maxAttempts; this.baseBackoff = baseBackoff;
    this.producerId = producerId;
  }

  @Override public Mono<Void> runOnce() {
    return claimBatch().collectList().flatMap(list -> {
      if (list.isEmpty()) return Mono.empty();
      return Flux.fromIterable(list).concatMap(this::deliverOne).then();
    });
  }

  @Override public Flux<OutboxRecord> stream() {
    return Flux.interval(Duration.ofMillis(250))
        .flatMap(t -> claimBatch())
        .flatMap(this::deliverOne, 1);
  }

  private Flux<OutboxRecord> claimBatch() {
    return Mono.fromCallable(() -> tx.execute(status -> {
      var now = Instant.now();
      var until = java.sql.Timestamp.from(now);
      var claimUntil = java.sql.Timestamp.from(now.plus(lease));
      List<Map<String,Object>> rows = jdbc.queryForList("""
        WITH cte AS (
          SELECT id FROM outbox
           WHERE shard_key = ? AND status IN ('PENDING','FAILED')
             AND next_attempt_at <= ?
           ORDER BY created_at
           LIMIT ?
           FOR UPDATE SKIP LOCKED
        )
        UPDATE outbox o SET status = 'CLAIMED', producer_id = ?, claimed_at = ?
        FROM cte WHERE o.id = cte.id
        RETURNING o.*
      """, shard, until, batchSize, producerId, claimUntil);
      return rows.stream().map(RowMappers::toRecord).collect(Collectors.toList());
    })).flatMapMany(Flux::fromIterable);
  }

  private Mono<OutboxRecord> deliverOne(OutboxRecord rec) {
    final byte[] bytes = rec.payload().getBytes(StandardCharsets.UTF_8);
    final Object decoded = serializer.deserialize(bytes, Object.class);

    final MessageEnvelope<?> msg;
    if (decoded instanceof MessageEnvelope<?> me) {
      // Elimina capture com cast explícito para <Object>
      @SuppressWarnings("unchecked")
      MessageEnvelope<Object> typed = (MessageEnvelope<Object>) me;
      msg = MessageMapper.fromEnvelope(rec, typed, serializer.contentType());
    } else if (decoded instanceof Map<?,?> any) {
      @SuppressWarnings("unchecked")
      Map<String,Object> ce = (Map<String,Object>) any; // chave e valor como Object são suficientes
      msg = MessageMapper.fromCloudEventsMap(rec, ce, serializer.contentType());
    } else {
      return onDeliveryError(rec, new IllegalArgumentException("Outbox payload inválido: " + decoded.getClass()));
    }

    return producer.send(msg, ProducerOptions.defaults())
        .then(Mono.fromRunnable(() ->
            tx.executeWithoutResult(st -> jdbc.update("UPDATE outbox SET status='SENT' WHERE id=?", rec.id()))
        ))
        .thenReturn(rec)                             // <<--- garante Mono<OutboxRecord>
        .onErrorResume(ex -> onDeliveryError(rec, ex));
  }

  private Mono<OutboxRecord> onDeliveryError(OutboxRecord rec, Throwable ex) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st -> {
      int attempt = rec.attempt() + 1;
      if (attempt >= maxAttempts) {
        jdbc.update("UPDATE outbox SET status='DEAD', attempt=?, error=? WHERE id=?",
            attempt, trim(ex), rec.id());
      } else {
        long jitter = (long)(Math.random()*1000);
        Duration next = baseBackoff.multipliedBy((long)Math.pow(2, attempt-1));
        jdbc.update("UPDATE outbox SET status='FAILED', attempt=?, error=?, next_attempt_at=? WHERE id=?",
            attempt, trim(ex), java.sql.Timestamp.from(Instant.now().plus(next).plusMillis(jitter)), rec.id());
      }
    })).thenReturn(rec);
  }

  private String trim(Throwable t){ var s = t.toString(); return s.length()>1000 ? s.substring(0,1000) : s; }

  static final class RowMappers {
    static OutboxRecord toRecord(Map<String,Object> r){
      return new OutboxRecord(
          (String) r.get("id"),
          (String) r.get("topic"),
          (String) r.get("msg_key"),
          (String) r.get("payload"),
          (String) r.get("content_type"),
          Map.of(),
          ((java.sql.Timestamp) r.get("created_at")).toInstant(),
          OutboxStatus.valueOf(((String) r.get("status")).toUpperCase()),
          ((Number) r.get("attempt")).intValue(),
          ((java.sql.Timestamp) r.get("next_attempt_at")).toInstant(),
          (String) r.get("error"),
          (String) r.get("shard_key"),
          (String) r.get("producer_id"),
          r.get("claimed_at")==null? null : ((java.sql.Timestamp) r.get("claimed_at")).toInstant()
      );
    }
  }
}
