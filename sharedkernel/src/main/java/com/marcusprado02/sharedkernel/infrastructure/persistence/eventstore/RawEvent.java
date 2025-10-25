package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.Map;

/** Evento “genérico” para desserialização quando não mapeamos o tipo exato. */
public  final class RawEvent implements DomainEvent {
  public Map<String, Object> data;
}
