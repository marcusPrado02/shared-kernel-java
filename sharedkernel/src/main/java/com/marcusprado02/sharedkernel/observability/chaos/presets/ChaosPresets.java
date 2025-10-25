package com.marcusprado02.sharedkernel.observability.chaos.presets;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import com.marcusprado02.sharedkernel.observability.chaos.ChaosPolicy;
import com.marcusprado02.sharedkernel.observability.chaos.actions.*;
import com.marcusprado02.sharedkernel.observability.chaos.conditions.*;

public final class ChaosPresets {
    private ChaosPresets(){}

    public static ChaosPolicy latencyOnOrders() {
        return ChaosPolicy.builder("orders-latency-p95", new LatencyJitter(100, 800))
                .when(new RouteMatchCondition("^/api/orders.*$"))
                .when(new PercentCondition(0.15))
                .blastRadius(0.10)                 // no max 10% dos requests elegíveis
                .ttl(Duration.ofHours(2))
                .requireToken(true)                // só com header de autorização
                .build();
    }

    public static ChaosPolicy throttleTenantStaging() {
        return ChaosPolicy.builder("staging-throttle-tenantA", new Throttle(200))
                .when(new TenantAllowlistCondition(Set.of("tenantA")))
                .when(new WindowCondition(java.time.LocalTime.of(0,0), java.time.LocalTime.of(6,0)))
                .blastRadius(0.5)
                .build();
    }

    public static ChaosPolicy exception10Percent() {
        return ChaosPolicy.builder("global-exception-10p", new ThrowException("Injected failure"))
                .when(new PercentCondition(0.10))
                .blastRadius(0.1).build();
    }
}
