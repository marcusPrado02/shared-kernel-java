package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.jdbc;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;


import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.ResultSet;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class JdbcEventStoreAdapter extends AbstractEventStoreAdapter {

  private final JdbcTemplate jdbc;
  private final TransactionTemplate tx;

  public JdbcEventStoreAdapter(
      JdbcTemplate jdbc,
      TransactionTemplate tx,
      EventSerializer serializer,
      List<EventUpcaster> upcasters,
      IdempotencyStore idempotency,
      OutboxPublisher outbox,
      MeterRegistry metrics,
      Tracer tracer,
      CircuitBreaker cbAppend, Retry retryAppend,
      CircuitBreaker cbRead, Retry retryRead
  ) {
    super(serializer, upcasters, idempotency, outbox, metrics, tracer, cbAppend, retryAppend, cbRead, retryRead);
    this.jdbc = jdbc;
    this.tx = tx;
  }

  @Override
  protected Mono<AppendResult> doAppend(String streamId, List<EventEnvelope<? extends DomainEvent>> events, AppendOptions options) {
    return Mono.fromCallable(() -> tx.execute(status -> {
      long current = currentStreamRevision(streamId);
      long base = switch (options.expectedRevision()) {
        case ExpectedRevision.Any a          -> current;
        case ExpectedRevision.NoStream n     -> { if (current != -1) throw new ConcurrencyException("Stream exists"); yield -1; }
        case ExpectedRevision.StreamExists s -> { if (current == -1) throw new ConcurrencyException("No stream"); yield current; }
        case ExpectedRevision.Exact e        -> { if (current != e.revision()) throw new ConcurrencyException("Mismatch"); yield current; }
      };
      long revision = base;
      for (var ev : events) {
        revision++;
        byte[] payload = serializer.serialize(ev);
        // Dica: para JSONB eficiente, serialize envelope -> Map/JSON e persista; aqui ilustrativo
        jdbc.update("""
           INSERT INTO events(stream_id, revision, event_id, event_type, event_version, occurred_at, tenant_id, dedup_key, payload, metadata)
           VALUES (?,?,?,?,?,?,?,?,
                   CAST(? AS JSONB),
                   CAST(? AS JSONB))
        """,
        streamId, revision, ev.eventId(), ev.eventType(), ev.eventVersion(), ev.occurredAt(),
        ev.metadata().tenantId(), ev.dedupKey(),
        new String(payload), // payload JSON
        toMetadataJson(ev.metadata())
        );
      }
      return new AppendResult(streamId, revision, events.size());
    }));
  }

  @Override
  protected Flux<EventEnvelope<?>> doReadStream(String streamId, ReadOptions options) {
    String order = options.direction() == ReadDirection.FORWARD ? "ASC" : "DESC";
    return Flux.defer(() -> {
      List<EventEnvelope<?>> list = jdbc.query("""
        SELECT event_id, stream_id, revision, occurred_at, event_type, event_version, dedup_key, payload, metadata
        FROM events
        WHERE stream_id=? AND revision >= ?
        ORDER BY revision """ + order + " LIMIT ?",
        (ResultSet rs, int rowNum) -> fromRow(rs),
        streamId, options.fromRevisionInclusive(), options.maxCount()
      );
      return Flux.fromIterable(list);
    });
  }

  @Override
  protected Flux<EventEnvelope<?>> doReadAll(GlobalPosition from, int batchSize) {
    return Flux.defer(() -> {
      List<EventEnvelope<?>> list = jdbc.query("""
        SELECT event_id, stream_id, revision, occurred_at, event_type, event_version, dedup_key, payload, metadata
        FROM events
        WHERE global_position >= ?
        ORDER BY global_position ASC
        LIMIT ?""",
        (ResultSet rs, int rowNum) -> fromRow(rs),
        from.value(), batchSize
      );
      return Flux.fromIterable(list);
    });
  }

  @Override
  protected Flux<EventEnvelope<?>> doSubscribe(SubscriptionOptions options) {
    // Em JDBC puro não há push nativo; estratégia: polling com backoff.
    // Em produção, considere NOTIFY/LISTEN (Postgres) ou Debezium (CDC).
    return Flux.interval(java.time.Duration.ofMillis(250))
        .onBackpressureDrop()
        .scan(options.fromPosition(), (pos, tick) -> new GlobalPosition(pos.value()))
        .flatMap(pos -> readAll(pos, options.batchSize()))
        .distinct(EventEnvelope::eventId); // proteção simples contra replay
  }

  @Override
  protected long currentStreamRevision(String streamId) {
    Long rev = jdbc.queryForObject("SELECT MAX(revision) FROM events WHERE stream_id=?", Long.class, streamId);
    return rev == null ? -1 : rev;
  }

  private EventEnvelope<?> fromRow(ResultSet rs) throws java.sql.SQLException {
    byte[] payloadBytes = rs.getString("payload").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    // Desserialize genericamente; em pipelines reais, resolva payloadType por registry
    EventEnvelope<?> env = serializer.deserialize(payloadBytes, RawEvent.class); // RawEvent implementa DomainEvent
    // Reaplique dados do DB (source of truth)
    return EventEnvelope.builder()
        .streamId(rs.getString("stream_id"))
        .revision(rs.getLong("revision"))
        .occurredAt(rs.getTimestamp("occurred_at").toInstant())
        .eventType(rs.getString("event_type"))
        .eventVersion(rs.getInt("event_version"))
        .dedupKey(rs.getString("dedup_key"))
        .payload((DomainEvent) env.payload())
        .metadata(fromMetadataJson(rs.getString("metadata")))
        .build();
  }

  private String toMetadataJson(EventMetadata md) { return Jsons.toJson(md); }
  private EventMetadata fromMetadataJson(String json) { return Jsons.fromJson(json, EventMetadata.class); }

    // --- ADMIN ---

  @Override
  public Mono<Void> deleteStream(String streamId, boolean hardDelete) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st -> {
      if (hardDelete) {
        jdbc.update("DELETE FROM events WHERE stream_id = ?", streamId);
      } else {
        // Soft-delete simples: marca metadado _deleted=true (opcional)
        jdbc.update("""
          UPDATE events 
             SET metadata = COALESCE(metadata, '{}'::jsonb) || '{"_deleted": true}'::jsonb
           WHERE stream_id = ?
        """, streamId);
      }
    }));
  }

  @Override
  public Mono<Void> truncateStream(String streamId, long truncateBeforeRevision) {
    return Mono.fromRunnable(() -> tx.executeWithoutResult(st -> {
      jdbc.update("DELETE FROM events WHERE stream_id = ? AND revision < ?", streamId, truncateBeforeRevision);
    }));
  }

  @Override
  public Mono<GlobalPosition> currentGlobalPosition() {
    return Mono.fromCallable(() -> {
      Long pos = jdbc.queryForObject("SELECT MAX(global_position) FROM events", Long.class);
      long v = (pos == null ? 0L : pos);
      return new GlobalPosition(v);
    });
  }
}