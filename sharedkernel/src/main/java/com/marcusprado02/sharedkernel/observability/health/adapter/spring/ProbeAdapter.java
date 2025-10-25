package com.marcusprado02.sharedkernel.observability.health.adapter.spring;

import org.springframework.boot.actuate.health.*;

import com.marcusprado02.sharedkernel.observability.health.ProbeCheck;

import reactor.core.publisher.Mono;

public final class ProbeAdapter implements HealthIndicator {
    private final ProbeCheck check;
    public ProbeAdapter(ProbeCheck c){ this.check = c; }
    @Override public Health health() {
        var r = check.check();
        var b = Health.status(map(r.status())).withDetail("reason", r.reason()).withDetails(r.details());
        if (r.time()!=null) b.withDetail("elapsed", r.time().toMillis()+"ms");
        return b.build();
    }
    private org.springframework.boot.actuate.health.Status map(com.marcusprado02.sharedkernel.observability.health.Status s) {
        return switch (s) {
            case UP -> Status.UP;
            case DEGRADED -> new Status("DEGRADED");
            case DOWN -> Status.DOWN;
        };
    }
}

