package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.List;

/** Publicação opcional para outbox/brokers após commit. */
public interface OutboxPublisher {
  void publish(List<EventEnvelope<?>> committedEvents);
}