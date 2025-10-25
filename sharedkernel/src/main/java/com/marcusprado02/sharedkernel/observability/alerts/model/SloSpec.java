package com.marcusprado02.sharedkernel.observability.alerts.model;

import java.util.Map;

/** SLO baseado em taxa de erro/latência + orçamentos (error budgets) */
public final class SloSpec {
    public final String service;       // ex: orders-service
    public final String sloName;       // ex: http-availability
    public final double objective;     // 0.99
    public final String windowLong;    // ex: 30d
    public final Map<String,String> labels; // tags fixas (env, team, region)
    public SloSpec(String service, String sloName, double objective, String windowLong, Map<String,String> labels){
        this.service=service; this.sloName=sloName; this.objective=objective; this.windowLong=windowLong;
        this.labels = labels==null? Map.of(): Map.copyOf(labels);
    }
}