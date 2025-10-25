package com.marcusprado02.sharedkernel.infrastructure.telemetry.log;

import io.opentelemetry.api.trace.Span;
import org.slf4j.MDC;

public final class LogContext {
    public static void enrich() {
        var span = Span.current();
        if (span != null && span.getSpanContext().isValid()) {
            MDC.put("traceId", span.getSpanContext().getTraceId());
            MDC.put("spanId", span.getSpanContext().getSpanId());
        }
    }
    public static void clear(){ MDC.remove("traceId"); MDC.remove("spanId"); }
}
