package com.marcusprado02.sharedkernel.cqrs.queryhandler.ports;

import java.util.Optional;

public interface QueryCache {
    Optional<Object> get(String key);
    void put(String key, Object value, int ttlSeconds);
}

