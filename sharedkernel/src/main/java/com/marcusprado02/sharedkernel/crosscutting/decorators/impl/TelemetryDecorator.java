package com.marcusprado02.sharedkernel.crosscutting.decorators.impl;

import com.marcusprado02.sharedkernel.crosscutting.decorators.core.Port;
import com.marcusprado02.sharedkernel.crosscutting.decorators.core.PortDecorator;

public class TelemetryDecorator<I,O> extends PortDecorator<I,O> {
    private final io.opentelemetry.api.trace.Tracer tracer;
    private final io.opentelemetry.api.metrics.Meter meter;
    private final String opName;
    public TelemetryDecorator(Port<I,O> delegate, io.opentelemetry.api.trace.Tracer tracer,
                              io.opentelemetry.api.metrics.Meter meter, String opName) {
        super(delegate); this.tracer = tracer; this.meter = meter; this.opName = opName;
    }
    @Override
    public O execute(I input) throws Exception {
        var span = tracer.spanBuilder(opName).startSpan();
        long t0 = System.nanoTime();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("port.operation", opName);
            // se fizer sentido, serializar tags low-card
            return delegate.execute(input);
        } catch (Exception e) {
            span.recordException(e); throw e;
        } finally {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            meter.histogramBuilder("port.duration.ms").ofLongs().build().record(ms);
            span.setAttribute("duration.ms", ms); span.end();
        }
    }
}

