package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;


public interface RateLimiter {
    /** @return true se permitido, false se bloqueado. */
    boolean tryAcquire(String key);
    Map<String, String> debugState(String key);
}


