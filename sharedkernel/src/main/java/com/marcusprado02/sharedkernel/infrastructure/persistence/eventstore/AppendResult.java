package com.marcusprado02.sharedkernel.infrastructure.persistence.eventstore;

/** Resultado de append. */
public record AppendResult(
    String streamId,
    long nextExpectedRevision,          // última revisão confirmada no stream
    int eventsAppended
) {}