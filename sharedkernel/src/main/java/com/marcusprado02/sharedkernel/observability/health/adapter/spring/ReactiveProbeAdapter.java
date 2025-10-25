package com.marcusprado02.sharedkernel.observability.health.adapter.spring;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;

import com.marcusprado02.sharedkernel.observability.health.ProbeCheck;

import reactor.core.publisher.Mono;

public final class ReactiveProbeAdapter implements ReactiveHealthIndicator {
    private final ProbeCheck check;
    public ReactiveProbeAdapter(ProbeCheck c){ this.check = c; }
    @Override public Mono<Health> health() {
        return Mono.fromSupplier(() -> new ProbeAdapter(check).health());
    }
}
