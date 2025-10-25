package com.marcusprado02.sharedkernel.infrastructure.search;

import java.time.Duration;
import java.util.Optional;

// -------- Cache opcional --------
public interface CacheAdapter {
    <T> Optional<PageResult<T>> get(String key, Class<T> type);
    <T> void put(String key, PageResult<T> value, Duration ttl);
    void invalidate(String keyPrefix);
}
