package com.marcusprado02.sharedkernel.domain.exception.telemetry;

import com.marcusprado02.sharedkernel.domain.exception.domain.DomainException;

import io.micrometer.core.instrument.MeterRegistry;

public final class ErrorMetrics {
    private final MeterRegistry meter;
    public ErrorMetrics(MeterRegistry m){ this.meter = m; }
    public void record(DomainException ex){
        meter.counter("domain_exception_total",
                "code", ex.codeFqn(),
                "severity", ex.severity().name(),
                "retryability", ex.retryability().name()).increment();
    }
}

