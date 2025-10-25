package com.marcusprado02.sharedkernel.observability.chaos;

import java.time.Instant;
import java.util.Map;

/** Contexto observado para decidir se injeta caos. */
public final class ChaosContext {
    public final Instant now = Instant.now();
    public final String route;       // /api/orders, rpc method, etc.
    public final String method;      // HTTP method ou RPC
    public final String tenant;      // MDC/headers
    public final String user;        // MDC/headers
    public final String traceId;     // tracing
    public final Map<String,String> tags; // extras (env, region, version...)

    public ChaosContext(String route, String method, String tenant, String user, String traceId, Map<String,String> tags){
        this.route=route; this.method=method; this.tenant=tenant; this.user=user; this.traceId=traceId;
        this.tags = tags==null? Map.of() : Map.copyOf(tags);
    }
}
