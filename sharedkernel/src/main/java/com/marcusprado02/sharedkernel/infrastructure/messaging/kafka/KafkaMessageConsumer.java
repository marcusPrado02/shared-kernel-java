package com.marcusprado02.sharedkernel.infrastructure.messaging.kafka;

import com.marcusprado02.sharedkernel.infrastructure.messaging.AbstractMessageConsumer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.ConsumerOptions;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageProcessingContext;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageSerializer;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KafkaMessageConsumer extends AbstractMessageConsumer {
  private final org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> consumer;

  public KafkaMessageConsumer(org.apache.kafka.clients.consumer.KafkaConsumer<String, byte[]> consumer,
                              MessageSerializer serializer, MeterRegistry metrics, Tracer tracer) {
    super(serializer, metrics, tracer);
    this.consumer = consumer;
  }

  @Override protected <T> Flux<MessageEnvelope<T>> doSubscribe(String topic, Class<T> payloadType, ConsumerOptions opt) {
    consumer.subscribe(java.util.List.of(topic));
    return Flux.<MessageEnvelope<T>>create(sink -> {
      // thread dedicado de polling
      var thread = new Thread(() -> {
        try {
          while (!Thread.currentThread().isInterrupted()) {
            var records = consumer.poll(java.time.Duration.ofMillis(250));
            records.forEach(rec -> {
              var env = serializer.deserialize(rec.value(), payloadType);
              sink.next(env);
            });
          }
        } catch (Exception e) { sink.error(e); }
      }, "kafka-consumer-" + topic);
      thread.setDaemon(true); thread.start();
      sink.onCancel(() -> { try { consumer.wakeup(); } catch (Exception ignore) {} });
    }).onBackpressureBuffer();
  }

  @Override protected <T> MessageProcessingContext contextFor(MessageEnvelope<T> env, ConsumerOptions options) {
    // Exemplo simplificado: commits síncronos por lote devem ser feitos fora (track offset)
    return new MessageProcessingContext() {
      @Override public Mono<Void> ack() { return Mono.empty(); }
      @Override public Mono<Void> nack(Throwable t) { return Mono.error(t); }
      @Override public String messageId() { return env.messageId(); }
      @Override public String topic() { return env.topic(); }
      @Override public String key() { return env.key(); }
    };
  }

  @Override protected <T> Mono<Void> handleFailure(MessageEnvelope<T> env, Throwable ex, ConsumerOptions options) {
    metrics.counter("msg.consumer.fail", "topic", env.topic(), "type", ex.getClass().getSimpleName()).increment();
    // Estratégia recomendada: usar DLT (Dead Letter Topic) por connector/streams job ou produtor dedicado
    return Mono.empty();
  }
}
