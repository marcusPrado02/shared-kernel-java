package com.marcusprado02.sharedkernel.infrastructure.platform.idempotency;

import java.time.Duration;
import java.util.function.Supplier;

/** Implementação pode usar Redis, DB único com UNIQUE(idempotency_key), etc. */
public interface IdempotencyService {
    <T> T withIdempotency(String key, Duration ttl, Supplier<T> action);
}