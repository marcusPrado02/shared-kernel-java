package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

import java.util.Optional;

/** Upcaster em cadeia para evoluir esquemas de eventos. */
public interface EventUpcaster {
  /** Retorna Optional com envelope atualizado se aplicável; caso contrário, o original. */
  Optional<EventEnvelope<?>> tryUpcast(EventEnvelope<?> original);
}

