package com.marcusprado02.sharedkernel.observability.tracing.adapters.otel;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class OtelBridge {
  public static TraceContext fromCurrent() {
    var span = io.opentelemetry.api.trace.Span.current();
    var ctx  = io.opentelemetry.context.Context.current();
    var sc   = span.getSpanContext();
    if (!sc.isValid()) return TraceContext.root();
    var baggage = io.opentelemetry.api.baggage.Baggage.fromContext(ctx)
        .asMap().entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
            Map.Entry::getKey, e -> e.getValue().getValue()
        ));
    return new TraceContext(sc.getTraceId(), sc.getSpanId(), sc.isSampled(), baggage, null, Map.of("protocol","otel"));
  }

  public static void makeCurrent(TraceContext c) {
    if (c==null || !c.isValid()) return;
    var span = io.opentelemetry.api.trace.Span.wrap(
        io.opentelemetry.api.trace.SpanContext.createFromRemoteParent(
            c.traceId(), c.spanId(),
            Boolean.TRUE.equals(c.sampled()) ? io.opentelemetry.api.trace.TraceFlags.getSampled() : io.opentelemetry.api.trace.TraceFlags.getDefault(),
            io.opentelemetry.api.trace.TraceState.getDefault()
        )
    );
    var ctx = io.opentelemetry.context.Context.current().with(span);
    if (c.baggage()!=null && !c.baggage().isEmpty()) {
      var bagBuilder = io.opentelemetry.api.baggage.Baggage.builder();
      c.baggage().forEach(bagBuilder::put);
      ctx = ctx.with(bagBuilder.build());
    }
    ctx.makeCurrent(); // cuide do escopo try-with-resources em código chamador
  }
}
