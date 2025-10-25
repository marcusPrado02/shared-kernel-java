package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** Opções para append. */
public record AppendOptions(
    ExpectedRevision expectedRevision,
    boolean requireIdempotency,         // se true, dedupKey é obrigatório
    boolean returnAssignedRevision,     // retorna último revision
    boolean publishToOutbox             // cria outbox record para broker
) {
  public static AppendOptions optimistic(ExpectedRevision er) {
    return new AppendOptions(er, true, true, true);
  }
}

