package com.marcusprado02.sharedkernel.infrastructure.outbox;

import java.util.List;

import com.marcusprado02.sharedkernel.events.domain.DomainEvent;
public interface OutboxPublisher {
  void publish(List<? extends DomainEvent> events); // opcional: gravar em tabela outbox
}