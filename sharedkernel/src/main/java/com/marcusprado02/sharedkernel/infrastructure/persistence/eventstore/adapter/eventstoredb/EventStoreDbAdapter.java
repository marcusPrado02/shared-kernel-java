package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.eventstoredb;

import com.eventstore.dbclient.*;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.*;

// Adapter para EventStoreDB (gRPC). Requer coluna/semântica de global position apenas quando você usar "all".
// O ESDB já possui posição global nativa (Position).
public class EventStoreDbAdapter extends AbstractEventStoreAdapter {

  private final EventStoreDBClient client;

  public EventStoreDbAdapter(
      EventStoreDBClient client,
      EventSerializer serializer,
      List<EventUpcaster> upcasters,
      IdempotencyStore idempotency,
      OutboxPublisher outbox,
      MeterRegistry metrics,
      Tracer tracer,
      CircuitBreaker cbAppend, Retry retryAppend,
      CircuitBreaker cbRead,   Retry retryRead
  ) {
    super(serializer, upcasters, idempotency, outbox, metrics, tracer, cbAppend, retryAppend, cbRead, retryRead);
    this.client = client;
  }

  // -------------------- APPEND --------------------

  @Override
  protected Mono<AppendResult> doAppend(String streamId,
                                        List<EventEnvelope<? extends DomainEvent>> events,
                                        AppendOptions options) {

    final com.eventstore.dbclient.ExpectedRevision esExpected =
        switch (options.expectedRevision()) {
          case com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.ExpectedRevision.Any a ->
              com.eventstore.dbclient.ExpectedRevision.any();
          case com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.ExpectedRevision.NoStream n ->
              com.eventstore.dbclient.ExpectedRevision.noStream();
          case com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.ExpectedRevision.StreamExists s ->
              com.eventstore.dbclient.ExpectedRevision.streamExists();
          case com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.ExpectedRevision.Exact e ->
              com.eventstore.dbclient.ExpectedRevision.expectedRevision(e.revision());
          default -> com.eventstore.dbclient.ExpectedRevision.any(); // garante default
        };

    final List<EventData> esEvents = events.stream().map(this::toEsdbEvent).toList();

    return Mono.fromFuture(
      client.appendToStream(
            streamId,
            AppendToStreamOptions.get().expectedRevision(esExpected),
            esEvents.toArray(new EventData[0])
        )
    ).map(res -> new AppendResult(
        streamId,
        res.getNextExpectedRevision().toRawLong(),
        events.size()
    ));

  }

  private EventData toEsdbEvent(EventEnvelope<?> e) {
    // Serializamos o ENVELOPE inteiro como data binária (mais simples e robusto)
    final byte[] data = serializer.serialize(e);

    return EventData.builderAsBinary(
            e.eventType(),                    // tipo lógico
            data                              // payload (envelope)
        )
        .eventId(e.eventId() != null ? UUID.fromString(e.eventId()) : UUID.randomUUID())
        .build();
  }

  // -------------------- READ STREAM --------------------

  @Override
  protected Flux<EventEnvelope<?>> doReadStream(String streamId, ReadOptions options) {
    final Direction dir = options.direction() == ReadDirection.FORWARD ? Direction.Forwards : Direction.Backwards;

    // readStream retorna CompletableFuture<ReadStreamResult> (iterável de ResolvedEvent)
    ReadStreamOptions readOpts = ReadStreamOptions.get().maxCount(options.maxCount());
    if (dir == Direction.Forwards) {
      readOpts = readOpts.forwards();
    } else {
      readOpts = readOpts.backwards();
    }
    return Mono.fromFuture(client.readStream(streamId, readOpts))
        .flatMapMany(result -> Flux.fromIterable(result.getEvents()))
        .map(ResolvedEvent::getOriginalEvent)
        .map(this::fromEsdbEventRecord);
  }

  // -------------------- READ ALL --------------------

  @Override
  protected Flux<EventEnvelope<?>> doReadAll(GlobalPosition from, int batchSize) {
    // Em ESDB, "all" usa Position (commit + prepare). Se você só tem um long, use ambos iguais.
    final Position pos = new Position(from.value(), from.value());
    return Mono.fromFuture(client.readAll(
            ReadAllOptions.get().forwards().fromPosition(pos).maxCount(batchSize)))
        .flatMapMany(result -> Flux.fromIterable(result.getEvents()))
        .map(ResolvedEvent::getOriginalEvent)
        .map(this::fromEsdbEventRecord);
  }

  // -------------------- SUBSCRIBE --------------------
  @Override
protected Flux<EventEnvelope<?>> doSubscribe(SubscriptionOptions options) {
  final Position pos = new Position(options.fromPosition().value(), options.fromPosition().value());
  return Flux.create(sink -> {
    try {
      com.eventstore.dbclient.SubscriptionListener listener = new com.eventstore.dbclient.SubscriptionListener() {
        @Override public void onEvent(Subscription sub, ResolvedEvent event) {
          sink.next(fromEsdbEventRecord(event.getEvent()));
        }
        public void onCancelled() { sink.complete(); }
        public void onError(Throwable t) { sink.error(t); }
      };

      client.subscribeToAll(listener, SubscribeToAllOptions.get().fromPosition(pos))
            .whenComplete((sub, err) -> { if (err != null) sink.error(err); });
    } catch (Throwable t) {
      sink.error(t);
    }
  });
}



  // -------------------- HELPERS --------------------

  private EventEnvelope<?> fromEsdbEventRecord(RecordedEvent ev) {
    final byte[] data = ev.getEventData();
    final EventEnvelope<?> env = serializer.deserialize(data, RawEvent.class);

    return EventEnvelope.builder()
        .eventId(ev.getEventId().toString())
        .streamId(ev.getStreamId())
        .revision(ev.getRevision())
        .occurredAt(Instant.ofEpochMilli(ev.getCreated().toEpochMilli()))
        .eventType(ev.getEventType())
        .eventVersion(env.eventVersion())
        .payload((DomainEvent) env.payload())
        .metadata(env.metadata())
        .build();
  }


  /** Consulta a última revisão do stream (ou -1 se não existir). */
  @Override
  protected long currentStreamRevision(String streamId) {
    try {
      var result = client.readStream(streamId, ReadStreamOptions.get().backwards().maxCount(1)).get();
      var it = result.getEvents().iterator();
      if (!it.hasNext()) return -1;
      return it.next().getOriginalEvent().getRevision();
    } catch (Exception e) {
      return -1;
    }
  }

  // (1) Métodos faltantes do EventStoreAdapter
  @Override
  public Mono<Void> deleteStream(String streamId, boolean hardDelete) {
    if (hardDelete) {
      return Mono.fromFuture(client.tombstoneStream(streamId)).then();
    } else {
      return Mono.fromFuture(client.deleteStream(streamId)).then();
    }
  }

  @Override
  public Mono<Void> truncateStream(String streamId, long beforeRevision) {
    StreamMetadata metadata = new StreamMetadata();
    metadata.setTruncateBefore(beforeRevision);
    return Mono.fromFuture(client.setStreamMetadata(streamId, metadata)).then();
  }


  @Override
  public Mono<GlobalPosition> currentGlobalPosition() {
    return Mono.fromCallable(() -> {
      var res = client.readAll(ReadAllOptions.get().backwards().maxCount(1)).get();
      var it = res.getEvents().iterator();
      if (!it.hasNext()) return GlobalPosition.START;
      var ev = it.next().getOriginalEvent();
      var pos = ev.getPosition(); // Position (commit/prepare)
      return new GlobalPosition(pos.getCommitUnsigned());
    }).onErrorReturn(GlobalPosition.START);
  }

}
