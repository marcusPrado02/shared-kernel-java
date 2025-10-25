package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** Serialização pluggável. Suporte a JSON/Avro/Proto + CloudEvents. */
public interface EventSerializer {
  byte[] serialize(EventEnvelope<?> envelope);
  <E extends DomainEvent> EventEnvelope<E> deserialize(byte[] bytes, Class<E> payloadType);
  String contentType(); // ex.: "application/cloudevents+json"
}

