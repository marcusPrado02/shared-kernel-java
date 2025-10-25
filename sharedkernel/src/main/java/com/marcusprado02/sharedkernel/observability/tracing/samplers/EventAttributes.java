package com.marcusprado02.sharedkernel.observability.tracing.samplers;

import java.time.Instant;
import java.util.*;

public final class EventAttributes {
    public final EventKind kind;
    public final String name;                 // ex: "http.request", "db.query", "orders.create"
    public final Instant ts;
    public final String severity;            // INFO|WARN|ERROR|... (opcional)
    public final Map<String, Object> fields; // tags/labels (tenant, route, status, userAgent, etc.)
    public final String key;                 // chave estável p/ "per-key sampling" (ex: "route:/api/x")

    public EventAttributes(EventKind kind, String name, Instant ts, String severity, Map<String,Object> fields, String key) {
        this.kind = kind; this.name = name; this.ts = ts;
        this.severity = severity; this.fields = fields==null? Map.of() : Map.copyOf(fields);
        this.key = key;
    }

    public Object field(String k){ return fields.get(k); }
}
