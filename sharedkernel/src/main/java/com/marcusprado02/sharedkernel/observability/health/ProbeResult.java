package com.marcusprado02.sharedkernel.observability.health;

import java.time.Duration;
import java.util.Map;

public record ProbeResult(Status status, String reason, Map<String, Object> details, Duration time) {
    public static ProbeResult up(Duration t){ return new ProbeResult(Status.UP, "ok", Map.of(), t); }
    public static ProbeResult down(String r, Map<String,Object> d, Duration t){ return new ProbeResult(Status.DOWN, r, d, t); }
    public static ProbeResult degraded(String r, Map<String,Object> d, Duration t){ return new ProbeResult(Status.DEGRADED, r, d, t); }
}
