package com.marcusprado02.sharedkernel.cqrs.command;

import java.time.Instant;
import java.util.Map;

public class CommandContextBuilder {
    private Instant now = Instant.now();
    private String tenantId;
    private Map<String,Object> headers = Map.of();
    private String userId;
    private String traceparent;
    private String correlationId;

    public CommandContextBuilder now(Instant now) { this.now = now; return this; }
    public CommandContextBuilder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
    public CommandContextBuilder headers(Map<String,Object> headers) { this.headers = headers; return this; }
    public CommandContextBuilder userId(String userId) { this.userId = userId; return this; }
    public CommandContextBuilder traceparent(String traceparent) { this.traceparent = traceparent; return this; }
    public CommandContextBuilder correlationId(String correlationId) { this.correlationId = correlationId; return this; }

    public CommandContext build() {
        return CommandContext.of(now, tenantId, headers, userId, traceparent, correlationId);
    }
}
