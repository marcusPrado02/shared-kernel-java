package com.marcusprado02.sharedkernel.observability.tracing.adapters.http;

import java.io.IOException;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.*;

import jakarta.servlet.ServletException;

public final class TraceServletFilter implements jakarta.servlet.Filter {
  private final TracePropagator propagator;
  public TraceServletFilter(TracePropagator p){ this.propagator=p; }

  @Override public void doFilter(jakarta.servlet.ServletRequest req, jakarta.servlet.ServletResponse res, jakarta.servlet.FilterChain chain)
      throws IOException, ServletException {
    var http = (jakarta.servlet.http.HttpServletRequest) req;
    var ctx = propagator.extract(http, HttpHeadersCarrier.requestGetter());
    var effective = ensureOrCreate(ctx); // se inválido, cria traceId/spanId
    // MDC
    org.slf4j.MDC.put("trace_id", effective.traceId());
    org.slf4j.MDC.put("span_id",  effective.spanId());
    try {
      req.setAttribute("traceContext", effective);
      chain.doFilter(req, res);
    } finally {
      org.slf4j.MDC.clear();
    }
  }

  private static TraceContext ensureOrCreate(TraceContext c) {
    if (c!=null && c.isValid()) return c;
    return new TraceContext(genTraceId(), genSpanId(), Boolean.TRUE, Map.of(), null, Map.of("generated","true"));
  }
  private static String genTraceId(){ return java.util.UUID.randomUUID().toString().replace("-",""); }
  private static String genSpanId(){ return Long.toHexString(new java.util.Random().nextLong()); }
}

