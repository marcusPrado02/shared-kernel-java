package com.marcusprado02.sharedkernel.infrastructure.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.metrics.Meter;

public interface TelemetryExporter extends AutoCloseable {
    OpenTelemetry openTelemetry();      // raiz OTel p/ APIs
    Tracer tracer(String instrumentationName);
    Meter meter(String instrumentationName);
    MeterRegistry meterRegistry();      // Micrometer (para Spring/Actuator)
    void forceFlush();                  // flush imediato (traces/logs/métricas)
    @Override void close();             // shutdown ordenado
}
