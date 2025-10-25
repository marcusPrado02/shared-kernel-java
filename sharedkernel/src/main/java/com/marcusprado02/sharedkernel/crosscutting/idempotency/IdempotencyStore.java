package com.marcusprado02.sharedkernel.crosscutting.idempotency;

import java.util.Optional;

/** Loja de idempotência genérica (key -> resultado). */
public interface IdempotencyStore {
    Optional<Object> tryGet(String key);
    void put(String key, Object value, long ttlSeconds);
}

