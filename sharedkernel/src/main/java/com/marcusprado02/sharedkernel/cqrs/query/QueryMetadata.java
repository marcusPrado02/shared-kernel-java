package com.marcusprado02.sharedkernel.cqrs.query;


import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/** Metadados técnicos de leitura. */
public final class QueryMetadata {
    public final String correlationId;
    public final String tenantId;
    public final String userId;
    public final String traceparent;
    public final ConsistencyHint consistency;
    public final Map<String, String> attributes;
    public final Instant timestampUtc;

    private QueryMetadata(Builder b) {
        this.correlationId = b.correlationId; this.tenantId = b.tenantId; this.userId = b.userId;
        this.traceparent = b.traceparent; this.consistency = b.consistency == null ? ConsistencyHint.AT_MOST_STALE : b.consistency;
        this.attributes = b.attributes == null ? Map.of() : Collections.unmodifiableMap(b.attributes);
        this.timestampUtc = b.timestampUtc == null ? Instant.now() : b.timestampUtc;
    }
    public static Builder builder(){ return new Builder(); }
    public static final class Builder {
        private String correlationId, tenantId, userId, traceparent; private ConsistencyHint consistency;
        private Map<String,String> attributes; private Instant timestampUtc;
        public Builder correlationId(String v){ this.correlationId=v; return this; }
        public Builder tenantId(String v){ this.tenantId=v; return this; }
        public Builder userId(String v){ this.userId=v; return this; }
        public Builder traceparent(String v){ this.traceparent=v; return this; }
        public Builder consistency(ConsistencyHint v){ this.consistency=v; return this; }
        public Builder attributes(Map<String,String> v){ this.attributes=v; return this; }
        public Builder timestampUtc(Instant v){ this.timestampUtc=v; return this; }
        public QueryMetadata build(){ return new QueryMetadata(this); }
    }

    public String correlationId() { return correlationId; }
    public String tenantId() { return tenantId; }
    public String userId() { return userId; }
    public String traceparent() { return traceparent; }
    public ConsistencyHint consistency() { return consistency; }
    public Map<String, String> attributes() { return attributes; }
    public Instant timestampUtc() { return timestampUtc; }  
}
