package com.marcusprado02.sharedkernel.infrastructure.inbox;

import java.util.Map;

import reactor.core.publisher.Mono;

/** Contexto fornecido ao handler com utilidades. */
public interface ProcessingContext {
  String messageId();
  String topic();
  String key();
  Map<String,String> headers();
  // Acesso ao EventStore/Outbox/Repos/Clock etc — injete um façade
  <T> T get(Class<T> depType);
  /** Lança “skip” sem erro (para mensagens duplicadas já efetivadas). */
  default Mono<Void> skip() { return Mono.empty(); }
}

