package com.marcusprado02.sharedkernel.cqrs.bus;

public interface EventStore {
    /** Lê em ordem por (stream, sequence) ou particionado por "all-streams". */
    CloseableIterator<EventEnvelope> read(ReplayCursor from, long maxBatch);
    ReplayCursor current(); // último offset
}
