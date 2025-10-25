package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.time.Instant;
import java.util.Objects;

/** Envelope imutável com tudo que precisamos para persistir e reemitir. */
public record EventEnvelope<E extends DomainEvent>(
    String eventId,                // ULID/UUID
    String streamId,               // aggregateType-aggregateId
    long revision,                 // número de sequência no stream (0..n)
    Instant occurredAt,            // clock do produtor
    String eventType,              // redundante p/ indexação/queries
    int eventVersion,              // p/ upcasting
    String dedupKey,               // idempotência cross-stream se desejado
    E payload,                     // evento tipado
    EventMetadata metadata         // metadados padrão
) {
  public static <E extends DomainEvent> Builder<E> builder() { return new Builder<>(); }
  public static final class Builder<E extends DomainEvent> {
    private String eventId = Ulids.nextUlid();
    private String streamId;
    private long revision = -1;
    private Instant occurredAt = Instant.now();
    private String eventType;
    private int eventVersion = 1;
    private String dedupKey;
    private E payload;
    private EventMetadata metadata = EventMetadata.minimal();
    public Builder<E> eventId(String id) { this.eventId = Objects.requireNonNull(id); return this; }
    public Builder<E> streamId(String s) { this.streamId = Objects.requireNonNull(s); return this; }
    public Builder<E> revision(long r) { this.revision = r; return this; }
    public Builder<E> occurredAt(Instant t) { this.occurredAt = t; return this; }
    public Builder<E> eventType(String t) { this.eventType = t; return this; }
    public Builder<E> eventVersion(int v) { this.eventVersion = v; return this; }
    public Builder<E> dedupKey(String k) { this.dedupKey = k; return this; }
    public Builder<E> payload(E p) { this.payload = Objects.requireNonNull(p); return this; }
    public Builder<E> metadata(EventMetadata m) { this.metadata = Objects.requireNonNull(m); return this; }
    public EventEnvelope<E> build() {
      String type = eventType != null ? eventType : payload.eventType();
      int ver = eventVersion > 0 ? eventVersion : payload.eventVersion();
      return new EventEnvelope<>(eventId, streamId, revision, occurredAt, type, ver, dedupKey, payload, metadata);
    }
  }
}
