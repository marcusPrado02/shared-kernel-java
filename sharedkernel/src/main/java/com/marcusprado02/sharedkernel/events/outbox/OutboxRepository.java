package com.marcusprado02.sharedkernel.events.outbox;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutboxRepository {
    void save(OutboxRecord rec);               // transacional com a escrita do agregado
    List<OutboxRecord> fetchBatch(int limit, Instant now);
    void markSent(String id);
    void markFailed(String id, String message, Instant nextAttemptAt);
    Optional<OutboxRecord> get(String id);
}
