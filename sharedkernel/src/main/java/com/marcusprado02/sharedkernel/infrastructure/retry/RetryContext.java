package com.marcusprado02.sharedkernel.infrastructure.retry;


import java.time.*;
import java.util.*;

public final class RetryContext {
    private final String operation;          // "geocode", "charge", etc.
    private final String endpointKey;        // p/ métricas/quota (ex.: "mapbox.geocode")
    private final String tenant;             // escopo opcional (multi-tenant)
    private final UUID idempotencyKey;       // gerado/propagado p/ POSTs
    private final Instant startAt;
    private final Map<String,Object> attrs;  // livre (provider,status,body-size)

    public RetryContext(String operation, String endpointKey, String tenant, UUID idempotencyKey, Map<String,Object> attrs) {
        this.operation = operation;
        this.endpointKey = endpointKey;
        this.tenant = tenant;
        this.idempotencyKey = idempotencyKey;
        this.startAt = Instant.now();
        this.attrs = attrs==null? Map.of() : Map.copyOf(attrs);
    }
    
    public String operation() { return operation; }
    public String endpointKey() { return endpointKey; }
    public String tenant() { return tenant; }
    public UUID idempotencyKey() { return idempotencyKey; }
    public Instant startAt() { return startAt; }
    public Map<String, Object> attrs() { return Collections.unmodifiableMap(attrs); }
}

