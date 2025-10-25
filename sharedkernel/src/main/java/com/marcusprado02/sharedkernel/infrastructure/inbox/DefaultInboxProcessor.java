package com.marcusprado02.sharedkernel.infrastructure.inbox;

import com.marcusprado02.sharedkernel.infrastructure.messaging.MessageEnvelope;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

public class DefaultInboxProcessor implements InboxProcessor {

  private final InboxRepository repo;
  private final InboxPayloadDecoder decoder;
  private final HandlerRegistry registry;
  private final TransactionTemplate tx;     // TX do aplicativo (domínio+eventstore+outbox)
  private final MeterRegistry metrics;
  private final Tracer tracer;

  public DefaultInboxProcessor(InboxRepository repo,
                               InboxPayloadDecoder decoder,
                               HandlerRegistry registry,
                               TransactionTemplate tx,
                               MeterRegistry metrics,
                               Tracer tracer) {
    this.repo = repo; this.decoder = decoder; this.registry = registry;
    this.tx = tx; this.metrics = metrics; this.tracer = tracer;
  }

  @Override
  public <T> Mono<Void> onMessage(MessageEnvelope<T> message, Class<T> dtoType, ProcessingOptions opt) {
    var timer = io.micrometer.core.instrument.Timer.start(metrics);

    // 1) Upsert do registro na inbox
    return repo.putIfAbsent(new InboxRecord(
          message.messageId(),
          message.topic(),
          message.key(),
          Jsons.toJson(message),                      // armazena snapshot em JSON
          message.headers().schema(),                 // referência de schema (se houver)
          message.headers().kv(),                     // headers extras
          java.time.Instant.now(),
          InboxStatus.RECEIVED,
          0,
          java.time.Instant.now(),
          null
        ))
        .flatMap(rec -> {
          // 2) Se já processado, encerra (idempotência)
          if (rec.status() == InboxStatus.PROCESSED) {
            metrics.counter("inbox.duplicate", "topic", message.topic()).increment();
            return Mono.<Void>empty();
          }
          // 3) Tenta pegar o lock para processamento
          return repo.tryMarkProcessing(message.messageId())
              .flatMap(locked -> {
                if (!locked) return Mono.<Void>empty();

                // 4) Decodificação/validação
                T dto = message.payload();
                if (dto == null) {
                  // Se o envelope veio só com bytes/JSON, decodifica
                  dto = decoder.decode(
                      message.headers().schema(),
                      Jsons.toJson(message.payload()),
                      dtoType,
                      message.headers().kv()
                  );
                }
                if (opt.validateSchema() && opt.schemaRef().isPresent()) {
                  decoder.validate(
                      message.headers().schema(),
                      Jsons.toJson(message.payload()),
                      opt.schemaRef().get()
                  );
                }
                final T finalDto = dto;

                // 5) Executa handler dentro da transação do app
                return Mono.fromRunnable(() ->
                      tx.executeWithoutResult(st -> {
                        var handler = registry.resolve(message.topic(), dtoType);
                        handler.handle(new DefaultProcessingContext(message), finalDto).block();
                      })
                    )
                    // 6) Marca como processado ao final
                    .then(repo.markProcessed(message.messageId()))
                    .doOnSuccess(v ->
                        metrics.counter("inbox.processed", "topic", message.topic()).increment()
                    )
                    // 7) Em erro: marca FAILED/DEAD com backoff
                    .onErrorResume(ex ->
                        repo.markFailed(message.messageId(), ex.toString(), opt.maxAttempts(), opt.baseBackoff())
                           .then(Mono.fromRunnable(() ->
                               metrics.counter("inbox.failed", "topic", message.topic(), "type", ex.getClass().getSimpleName()).increment()
                           ))
                    );
              });
        })
        .doFinally(s -> timer.stop(metrics.timer("inbox.latency", "topic", message.topic())));
  }

  // ---------- Helpers ----------

  static final class DefaultProcessingContext implements ProcessingContext {
    private final MessageEnvelope<?> msg;
    DefaultProcessingContext(MessageEnvelope<?> msg){ this.msg = msg; }
    @Override public String messageId() { return msg.messageId(); }
    @Override public String topic() { return msg.topic(); }
    @Override public String key() { return msg.key(); }
    @Override public java.util.Map<String, String> headers() { return msg.headers().kv(); }
    @Override public <D> D get(Class<D> depType) { throw new UnsupportedOperationException("Wire your service locator/DI aqui"); }
  }

  static final class Jsons {
    static String toJson(Object o){
      try {
        return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(o);
      } catch(Exception e){ throw new RuntimeException(e); }
    }
  }
}
