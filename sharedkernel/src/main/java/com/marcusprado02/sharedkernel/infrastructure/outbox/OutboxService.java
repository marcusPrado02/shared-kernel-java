package com.marcusprado02.sharedkernel.infrastructure.outbox;

import java.util.Map;

import reactor.core.publisher.Mono;

public interface OutboxService {
  /** Chamado na camada de aplicação, DENTRO da tx do caso de uso. */
  Mono<Void> enqueue(String topic, String key, Object payload, Map<String,String> headers);
}