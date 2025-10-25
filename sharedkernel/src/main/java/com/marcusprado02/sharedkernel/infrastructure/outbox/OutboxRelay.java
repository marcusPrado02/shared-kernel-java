package com.marcusprado02.sharedkernel.infrastructure.outbox;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutboxRelay {
  /** Worker que busca pendentes (ou claimed expirados) e publica no broker. */
  Mono<Void> runOnce();           // útil p/ cron/k8s job
  Flux<OutboxRecord> stream();    // modo contínuo (daemon)
}
