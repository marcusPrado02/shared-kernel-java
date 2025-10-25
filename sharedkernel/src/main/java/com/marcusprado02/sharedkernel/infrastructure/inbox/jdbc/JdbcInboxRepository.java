package com.marcusprado02.sharedkernel.infrastructure.inbox.jdbc;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.marcusprado02.sharedkernel.infrastructure.inbox.InboxRecord;
import com.marcusprado02.sharedkernel.infrastructure.inbox.InboxRepository;
import com.marcusprado02.sharedkernel.infrastructure.inbox.InboxStatus;

import reactor.core.publisher.Mono;
import java.time.*;
import java.util.Map;
import java.util.Optional;

public class JdbcInboxRepository implements InboxRepository {
  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  public JdbcInboxRepository(JdbcTemplate jdbc, TransactionTemplate tx) {
    this.jdbc = jdbc; this.tx = tx;
  }

  @Override public Mono<InboxRecord> putIfAbsent(InboxRecord r) {
    return Mono.fromCallable(() -> tx.execute(status -> {
      try {
        jdbc.update("""
          INSERT INTO inbox (message_id, topic, msg_key, payload, content_type, headers, status)
          VALUES (?,?,?,?,?,?::jsonb,'RECEIVED')
        """, r.messageId(), r.topic(), r.key(), r.payload(), r.contentType(), Jsons.toJson(r.headers()));
      } catch (org.springframework.dao.DuplicateKeyException already) { /* ignore */ }
      return find(r.messageId()).block().orElseThrow();
    }));
  }

  @Override public Mono<Boolean> tryMarkProcessing(String messageId) {
    return Mono.fromCallable(() -> tx.execute(status ->
      jdbc.update("UPDATE inbox SET status='PROCESSING', attempt = attempt + 1 WHERE message_id=? AND status IN ('RECEIVED','FAILED')",
        messageId) == 1
    ));
  }

  @Override public Mono<Void> markProcessed(String messageId) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st ->
      jdbc.update("UPDATE inbox SET status='PROCESSED' WHERE message_id=?", messageId)
    ));
  }

  @Override public Mono<Void> markFailed(String messageId, String error, int maxAttempts, Duration baseBackoff) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st -> {
      var row = jdbc.queryForMap("SELECT attempt FROM inbox WHERE message_id=?", messageId);
      int attempt = ((Number)row.getOrDefault("attempt", 0)).intValue();
      attempt += 1;
      if (attempt >= maxAttempts) {
        jdbc.update("UPDATE inbox SET status='DEAD', attempt=?, error=? WHERE message_id=?",
            attempt, truncate(error, 2000), messageId);
      } else {
        long jitter = (long)(Math.random()*1000);
        Duration next = baseBackoff.multipliedBy((long)Math.pow(2, Math.max(0, attempt-1)));
        jdbc.update("UPDATE inbox SET status='FAILED', attempt=?, error=?, next_attempt_at=? WHERE message_id=?",
            attempt, truncate(error, 2000), java.sql.Timestamp.from(Instant.now().plus(next).plusMillis(jitter)), messageId);
      }
    }));
  }

  @Override public Mono<Void> markDead(String messageId, String error) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st ->
      jdbc.update("UPDATE inbox SET status='DEAD', error=? WHERE message_id=?", truncate(error, 2000), messageId)
    ));
  }

  @Override public Mono<Optional<InboxRecord>> find(String messageId) {
    return Mono.fromCallable(() -> {
      var list = jdbc.query("""
        SELECT message_id, topic, msg_key, payload, content_type, headers, received_at, status, attempt, next_attempt_at, error
        FROM inbox WHERE message_id=?
      """, (rs, i) -> new InboxRecord(
          rs.getString("message_id"), rs.getString("topic"), rs.getString("msg_key"),
          rs.getString("payload"), rs.getString("content_type"),
          Jsons.fromJson(rs.getString("headers"), Map.class),
          rs.getTimestamp("received_at").toInstant(),
          InboxStatus.valueOf(rs.getString("status")),
          rs.getInt("attempt"),
          rs.getTimestamp("next_attempt_at").toInstant(),
          rs.getString("error")
      ), messageId);
      return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    });
  }

  private static String truncate(String s, int max){ if (s==null) return null; return s.length()>max? s.substring(0,max): s; }

  // util simples; troque por ObjectMapper/Json-B em produção
  static final class Jsons {
    static String toJson(Object o){ try { return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o);} catch(Exception e){ throw new RuntimeException(e);} }
    @SuppressWarnings("unchecked")
    static <T> T fromJson(String j, Class<T> t){ try { return new com.fasterxml.jackson.databind.ObjectMapper().readValue(j, t);} catch(Exception e){ throw new RuntimeException(e);} }
  }
}

