package com.marcusprado02.sharedkernel.observability.alerts.model;

import java.util.*;

public final class AlertRule {
    public final String alert;                 // Alert name
    public final String expr;                  // expr final (ex: PromQL)
    public final String forDuration;           // hold
    public final Map<String,String> labels;    // severity, service, team, route...
    public final Map<String,String> annotations; // summary, description, runbook
    public AlertRule(String alert, String expr, String forD, Map<String,String> labels, Map<String,String> annotations){
        this.alert=alert; this.expr=expr; this.forDuration=forD;
        this.labels = labels==null? Map.of(): Map.copyOf(labels);
        this.annotations = annotations==null? Map.of(): Map.copyOf(annotations);
    }
}