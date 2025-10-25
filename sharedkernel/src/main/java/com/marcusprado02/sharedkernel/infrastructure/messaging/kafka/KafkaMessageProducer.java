package com.marcusprado02.sharedkernel.infrastructure.messaging.kafka;

import org.apache.kafka.clients.producer.*;

import com.marcusprado02.sharedkernel.infrastructure.messaging.AbstractMessageProducer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageSerializer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.ProducerOptions;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Mono;

public class KafkaMessageProducer extends AbstractMessageProducer {
  private final Producer<String, byte[]> producer;

  public KafkaMessageProducer(Producer<String, byte[]> producer, MessageSerializer serializer,
                              MeterRegistry metrics, Tracer tracer, Retry retry, CircuitBreaker cb) {
    super(serializer, metrics, tracer, retry, cb);
    this.producer = producer;
  }

  @Override protected <T> Mono<Void> doSend(MessageEnvelope<T> m, ProducerOptions opt) {
    return Mono.create(sink -> {
      var record = new ProducerRecord<>(m.topic(), opt.partition().orElse(null), m.occurredAt().toEpochMilli(), m.key(),
          serializer.serialize(m));
      record.headers().add("content-type", serializer.contentType().getBytes());
      if (m.headers().correlationId()!=null) record.headers().add("correlation-id", m.headers().correlationId().getBytes());
      // Transacional (opcional): beginTransaction em higher-level (outbox)
      producer.send(record, (meta, ex) -> {
        if (ex != null) sink.error(ex); else sink.success();
      });
    });
  }
}
