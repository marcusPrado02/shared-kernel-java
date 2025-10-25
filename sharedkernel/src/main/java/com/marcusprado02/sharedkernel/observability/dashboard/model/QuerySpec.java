package com.marcusprado02.sharedkernel.observability.dashboard.model;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.dashboard.core.Backend;

public final class QuerySpec {
    public final Backend backend;
    public final String expr;                 // PromQL/LogQL/KQL
    public final String legend;
    public final String interval;             // ex.: "1m"
    public final Map<String,String> labels;   // rótulos auxiliares

    public QuerySpec(Backend backend, String expr, String legend, String interval, Map<String,String> labels){
        this.backend=backend; this.expr=expr; this.legend=legend; this.interval=interval;
        this.labels = labels==null? Map.of(): Map.copyOf(labels);
    }
}