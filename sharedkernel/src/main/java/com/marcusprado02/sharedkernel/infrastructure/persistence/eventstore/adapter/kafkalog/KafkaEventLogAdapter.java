package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.kafkalog;

import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * Adaptador de EventLog em Kafka.
 * Nota: Kafka não é um event-store transacional por stream; aqui tratamos como "log de eventos"
 * por tópico/partição. Operações como delete/truncate são limitadas (dependem de policies do broker).
 */
public class KafkaEventLogAdapter extends AbstractEventStoreAdapter {

  private final KafkaProducer<String, byte[]> producer;
  private final String defaultTopic;

  /**
   * @param kafkaProps propriedades do KafkaProducer (acks=all, enable.idempotence=true recomendados)
   * @param defaultTopic tópico default para eventos quando não houver mapping por stream
   */
  public KafkaEventLogAdapter(
      Properties kafkaProps,
      String defaultTopic,
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
    this.producer = new KafkaProducer<>(Objects.requireNonNull(kafkaProps, "kafkaProps"));
    this.defaultTopic = Objects.requireNonNull(defaultTopic, "defaultTopic");
  }

  // ------------------------------------------------------------------------------------
  // APPEND
  // ------------------------------------------------------------------------------------

  @Override
  protected Mono<AppendResult> doAppend(String streamId,
                                        List<EventEnvelope<? extends DomainEvent>> events,
                                        AppendOptions options) {
    // Mapeamento simplificado: 1 streamId -> 1 tópico (ou defaultTopic)
    final String topic = mapStreamToTopic(streamId);

    // Serializa e envia cada envelope como uma mensagem Kafka (key = streamId)
    return Mono.fromCallable(() -> {
      long nextRevision = currentStreamRevision(streamId); // semântica fraca no Kafka; retornamos -1 por padrão
      for (var ev : events) {
        byte[] payload = serializer.serialize(ev);
        var record = new ProducerRecord<>(topic, streamId, payload);
        // Envio síncrono (simplificado). Em produção, use callbacks/futuros + retry de produtor.
        producer.send(record).get();
        nextRevision++; // mero contador local; em Kafka use offset por partição se quiser precisão
      }
      return new AppendResult(streamId, nextRevision, events.size());
    });
  }

  // ------------------------------------------------------------------------------------
  // READ STREAM
  // ------------------------------------------------------------------------------------

  @Override
  protected Flux<EventEnvelope<?>> doReadStream(String streamId, ReadOptions options) {
    // TODO: implementar com KafkaConsumer: assign() na partição mapeada para o stream,
    // seek() para o offset equivalente à revisão, e gerar envelopes desserializando serializer.deserialize(...)
    // Por enquanto, retornamos Flux vazio para compilar.
    return Flux.empty();
  }

  // ------------------------------------------------------------------------------------
  // READ ALL (GLOBAL)
  // ------------------------------------------------------------------------------------

  @Override
  protected Flux<EventEnvelope<?>> doReadAll(GlobalPosition from, int batchSize) {
    // TODO: implementar leitura do(s) tópico(s) de eventos a partir de offsets >= from.value()
    // Por enquanto, Flux vazio.
    return Flux.empty();
  }

  // ------------------------------------------------------------------------------------
  // SUBSCRIBE
  // ------------------------------------------------------------------------------------

  @Override
  protected Flux<EventEnvelope<?>> doSubscribe(SubscriptionOptions options) {
    // TODO: implementar com KafkaConsumer em loop (poll) adaptado para Flux.create(...)
    // e commit assíncrono conforme autoAck/durável.
    return Flux.empty();
  }

  // ------------------------------------------------------------------------------------
  // METADADOS / AUXILIARES
  // ------------------------------------------------------------------------------------

  /** Em Kafka puro, não há "revisão do stream" canônica. Retorne -1 para "desconhecida". */
  @Override
  protected long currentStreamRevision(String streamId) {
    // TODO: se você mapear stream -> (topic, partition), pode consultar endOffsets e inferir.
    return -1;
  }

  /** Kafka não expõe uma posição global única; devolva um cursor sintético ou START. */
  @Override
  public Mono<GlobalPosition> currentGlobalPosition() {
    // TODO: se precisar, agregue endOffsets e derive um valor; por ora, START.
    return Mono.just(GlobalPosition.START);
  }

  /** Truncar um stream não é operação nativa no Kafka. */
  @Override
  public Mono<Void> truncateStream(String streamId, long toRevisionInclusive) {
    return Mono.error(new UnsupportedOperationException("truncateStream não é suportado em Kafka"));
  }

  /** Deletar stream (mensagens) depende de policies de retenção/compactação. */
  @Override
  public Mono<Void> deleteStream(String streamId, boolean hardDelete) {
    return Mono.error(new UnsupportedOperationException("deleteStream não é suportado em Kafka"));
  }

  // ------------------------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------------------------

  private String mapStreamToTopic(String streamId) {
    // Estratégia simplificada. Em produção, mapeie por aggregate/type/tenant:
    // ex.: "orders-evt", "payments-evt", etc.
    return defaultTopic;
  }
}
