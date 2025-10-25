package com.marcusprado02.sharedkernel.adapters.in.sse.core;


import java.time.Instant;
import java.util.Map;

public record SseMessage(
    String id,           // ULID/UUIDv7
    String event,        // optional
    String data,         // JSON compacto
    Integer retryMs,     // optional
    Instant ts,
    Map<String,String> attributes // tenant, userId, topic, etc.
) {}

