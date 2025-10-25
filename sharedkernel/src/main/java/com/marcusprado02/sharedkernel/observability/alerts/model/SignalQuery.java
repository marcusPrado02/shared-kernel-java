package com.marcusprado02.sharedkernel.observability.alerts.model;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.alerts.core.SignalBackend;

public final class SignalQuery {
    public final SignalBackend backend;
    public final String expr;            // PromQL/LogQL/KQL/MQL...
    public final String window;          // ex: 5m, 1h, 30d
    public final Map<String, String> labels; // complements

    public SignalQuery(SignalBackend backend, String expr, String window, Map<String,String> labels) {
        this.backend = backend; this.expr = expr; this.window = window; this.labels = labels==null? Map.of(): Map.copyOf(labels);
    }
}
