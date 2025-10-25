package com.marcusprado02.sharedkernel.observability.metrics.factory;

import com.marcusprado02.sharedkernel.observability.metrics.adapters.NoopMetricsAdapter;
import com.marcusprado02.sharedkernel.observability.metrics.adapters.micrometer.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.metrics.impl.MetricsFacadeImpl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

public final class MetricsFacadeFactory {
    private MetricsFacadeFactory(){}

    public static MetricsFacade noop() {
        return new MetricsFacadeImpl(new NoopMetricsAdapter());
    }

    /** Micrometer direto (injete MeterRegistry do Spring) */
    public static MetricsFacade micrometer(MeterRegistry registry) {
        var adapter = new MicrometerAdapter(registry, NameSanitizer.prometheusSafe(), CardinalityLimiter.permissive());
        return new MetricsFacadeImpl(adapter);
    }

    /** Fallback simples (tenta Micrometer via reflexão; senão Noop). */
    public static MetricsFacade auto() {
        try {
            Class.forName("io.micrometer.core.instrument.MeterRegistry");
            var global = (Metrics.globalRegistry);
            return micrometer(global);
        } catch (Throwable ignore) {
            return noop();
        }
    }
}
