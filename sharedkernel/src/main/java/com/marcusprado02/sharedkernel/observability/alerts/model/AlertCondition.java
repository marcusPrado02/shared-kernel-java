package com.marcusprado02.sharedkernel.observability.alerts.model;

import com.marcusprado02.sharedkernel.observability.alerts.core.AlertSeverity;

public final class AlertCondition {
    public final String name;            // ex: http_errors_ratio
    public final SignalQuery query;
    public final String comparison;      // >, <, >=, <=
    public final double threshold;       // ex: 0.01 (1%)
    public final String forDuration;     // ex: "5m" (hold)
    public final AlertSeverity severity;

    public AlertCondition(String name, SignalQuery q, String cmp, double thr, String forD, AlertSeverity sev){
        this.name=name; this.query=q; this.comparison=cmp; this.threshold=thr; this.forDuration=forD; this.severity=sev;
    }
}
