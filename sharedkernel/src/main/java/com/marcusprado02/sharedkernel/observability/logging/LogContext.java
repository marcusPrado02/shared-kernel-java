package com.marcusprado02.sharedkernel.observability.logging;

import java.time.Instant;
import java.util.*;

public final class LogContext {
    public final String service;     // ex: orders-service
    public final String environment; // ex: prod/stage
    public final String region;      // ex: sa-east-1
    public final String version;     // ex: 1.4.2
    public final Map<String,String> staticTags; // sempre presentes

    public LogContext(String service, String environment, String region, String version, Map<String,String> staticTags) {
        this.service = service; this.environment = environment; this.region = region; this.version = version;
        this.staticTags = staticTags==null? Map.of() : Map.copyOf(staticTags);
    }

    public static LogContext minimal(String service){ return new LogContext(service, "unknown", "unknown", "unknown", Map.of()); }
}