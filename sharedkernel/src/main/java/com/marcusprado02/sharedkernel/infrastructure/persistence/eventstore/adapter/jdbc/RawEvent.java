package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.adapter.jdbc;

import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore.DomainEvent;


/** Evento “genérico” para desserialização quando o tipo exato não é conhecido. */
public class RawEvent implements DomainEvent {
  public Map<String, Object> data;
}