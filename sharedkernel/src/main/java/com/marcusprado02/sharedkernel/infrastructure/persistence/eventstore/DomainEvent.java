package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** Marcador para eventos de domínio. Evite lógica aqui. */
public interface DomainEvent {
  /** Nome lógico (ex.: "PaymentAuthorized"). Default: FQN simples. */
  default String eventType() { return getClass().getSimpleName(); }
  /** Versão do schema do evento para upcasting. */
  default int eventVersion() { return 1; }
}