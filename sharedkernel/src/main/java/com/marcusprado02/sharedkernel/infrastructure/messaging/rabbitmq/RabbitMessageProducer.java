package com.marcusprado02.sharedkernel.infrastructure.messaging.rabbitmq;

import com.marcusprado02.sharedkernel.infrastructure.messaging.AbstractMessageProducer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageSerializer;
import com.marcusprado02.sharedkernel.infrastructure.messaging.ProducerOptions;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import reactor.core.publisher.Mono;

/**
 * Producer RabbitMQ com confirmação básica.
 * Assume exchange topic (default "amq.topic") e routingKey = m.topic().
 */
public class RabbitMessageProducer extends AbstractMessageProducer {

  private final RabbitTemplate rabbit;
  private final MessageSerializer serializer;

  public RabbitMessageProducer(RabbitTemplate rabbit,
                               MessageSerializer serializer,
                               MeterRegistry metrics,
                               Tracer tracer,
                               Retry retry,
                               CircuitBreaker cb) {
    super(serializer, metrics, tracer, retry, cb);
    this.rabbit = rabbit;
    this.serializer = serializer;

    // Recomendações operacionais:
    this.rabbit.setMandatory(true); // retorna unroutable (Basic.Return)
  }

  @Override
  protected <T> Mono<Void> doSend(MessageEnvelope<T> m, ProducerOptions opt) {
    // 👉 Como ProducerOptions não tem exchange(), use padrão "amq.topic"
    final String exchange = "amq.topic";
    final String routingKey = m.topic();

    return Mono.fromRunnable(() -> {
      byte[] body = serializer.serialize(m);

      MessagePostProcessor headers = msg -> {
        var props = msg.getMessageProperties();
        props.setContentType(serializer.contentType());
        props.setMessageId(m.messageId());
        props.setHeader("correlation-id", m.headers().correlationId());
        props.setHeader("key", m.key());
        m.headers().kv().forEach(props::setHeader);
        return msg;
      };

      rabbit.convertAndSend(exchange, routingKey, body, headers);
    });
  }
}