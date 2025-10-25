package com.marcusprado02.sharedkernel.cqrs.command;


import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Metadados técnicos que acompanham o Command. */
public final class CommandMetadata {
    public final CommandId commandId;
    public final String correlationId;
    public final String causationId;
    public final String idempotencyKey;
    public final String tenantId;
    public final String userId;
    public final Instant timestampUtc;
    public final Instant executeAtUtc; // agendamento opcional
    public final int priority;         // 0=normal; maior = mais prioritário
    public final RetryPolicy retryPolicy;
    public final String traceparent;   // W3C trace-context
    public final Set<String> tags; // para roteamento (DLT, retry, etc)
    public final Map<String, Object> headers;
    public final Map<String, String> attributes; // extensões

    private CommandMetadata(Builder b) {
        this.commandId = Objects.requireNonNullElseGet(b.commandId, CommandId::newRandom);
        this.correlationId = b.correlationId;
        this.causationId = b.causationId;
        this.idempotencyKey = b.idempotencyKey;
        this.tenantId = b.tenantId;
        this.userId = b.userId;
        this.timestampUtc = Objects.requireNonNullElseGet(b.timestampUtc, Instant::now);
        this.executeAtUtc = b.executeAtUtc;
        this.priority = b.priority;
        this.retryPolicy = Objects.requireNonNullElseGet(b.retryPolicy, RetryPolicy::disabled);
        this.traceparent = b.traceparent;
        this.attributes = b.attributes == null ? Map.of() : Collections.unmodifiableMap(b.attributes);
        this.tags = Set.of();
        this.headers = Map.of();
    }

    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private CommandId commandId;
        private String correlationId, causationId, idempotencyKey, tenantId, userId, traceparent;
        private Instant timestampUtc, executeAtUtc;
        private int priority = 0;
        private RetryPolicy retryPolicy;
        private Map<String,String> attributes;
        private Set<String> tags;
        private Map<String,Object> headers;

        public Builder commandId(CommandId v){ this.commandId=v; return this; }
        public Builder correlationId(String v){ this.correlationId=v; return this; }
        public Builder causationId(String v){ this.causationId=v; return this; }
        public Builder idempotencyKey(String v){ this.idempotencyKey=v; return this; }
        public Builder tenantId(String v){ this.tenantId=v; return this; }
        public Builder userId(String v){ this.userId=v; return this; }
        public Builder timestampUtc(Instant v){ this.timestampUtc=v; return this; }
        public Builder executeAtUtc(Instant v){ this.executeAtUtc=v; return this; }
        public Builder priority(int v){ this.priority=v; return this; }
        public Builder retryPolicy(RetryPolicy v){ this.retryPolicy=v; return this; }
        public Builder traceparent(String v){ this.traceparent=v; return this; }
        public Builder attributes(Map<String,String> v){ this.attributes=v; return this; }
        public Builder tags(Set<String> v){ this.tags=v; return this; }
        public Builder headers(Map<String,Object> v){ this.headers=v; return this; }
        public Builder tag(String v){
            if (this.tags == null) this.tags = new HashSet<>();
            this.tags.add(v); return this;
        }
        public Builder header(String k, Object v){
            if (this.headers == null) this.headers = new HashMap<>();
            this.headers.put(k, v); return this;
        }
        public CommandMetadata build(){ return new CommandMetadata(this); }
    }

    public CommandId commandId(){ return commandId; }
    public String correlationId(){ return correlationId; }
    public String causationId(){ return causationId; }
    public String idempotencyKey(){ return idempotencyKey; }
    public String tenantId(){ return tenantId; }
    public String userId(){ return userId; }
    public Instant timestampUtc(){ return timestampUtc; }
    public Instant executeAtUtc(){ return executeAtUtc; }
    public int priority(){ return priority; }
    public RetryPolicy retryPolicy(){ return retryPolicy; }
    public String traceparent(){ return traceparent; }
    public Map<String,String> attributes(){ return attributes; }
    public Set<String> tags(){ return tags; }
    public Map<String,Object> headers(){ return headers; }
}


