package com.marcusprado02.sharedkernel.cqrs.command;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/** Contexto técnico útil ao Handler (clock, identidade, tenant, etc.). */
public interface CommandContext {
    Instant now();
    Optional<String> tenantId();
    Map<String,Object> headers();
    Optional<String> userId();
    Optional<String> traceparent();
    Optional<String> correlationId();
    <T> Optional<T> attribute(String key, Class<T> type);

    static CommandContext empty() {
        return new CommandContext() {
            @Override public Instant now() { return Instant.now(); }
            @Override public Optional<String> tenantId() { return Optional.empty(); }
            @Override public Map<String, Object> headers() { return Map.of(); }
            @Override public Optional<String> userId() { return Optional.empty(); }
            @Override public Optional<String> traceparent() { return Optional.empty(); }
            @Override public Optional<String> correlationId() { return Optional.empty(); }
            @Override public <T> Optional<T> attribute(String key, Class<T> type) { return Optional.empty(); }
        };
    }

    static CommandContext of(Instant now, String tenantId, Map<String,Object> headers, String userId, String traceparent, String correlationId) {
        return new CommandContext() {
            @Override public Instant now() { return now; }
            @Override public Optional<String> tenantId() { return Optional.ofNullable(tenantId); }
            @Override public Map<String, Object> headers() { return headers != null ? headers : Map.of(); }
            @Override public Optional<String> userId() { return Optional.ofNullable(userId); }
            @Override public Optional<String> traceparent() { return Optional.ofNullable(traceparent); }
            @Override public Optional<String> correlationId() { return Optional.ofNullable(correlationId); }
            @Override public <T> Optional<T> attribute(String key, Class<T> type) { return Optional.empty(); }
        };
    }

    static CommandContextBuilder builder() { return new CommandContextBuilder(); }
}