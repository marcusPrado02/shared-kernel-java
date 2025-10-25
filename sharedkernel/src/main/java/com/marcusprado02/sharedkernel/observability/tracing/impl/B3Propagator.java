package com.marcusprado02.sharedkernel.observability.tracing.impl;

import java.util.Map;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class B3Propagator implements TracePropagator {
  private static final String B3_SINGLE = "b3";
  private static final String B3_TRACEID = "X-B3-TraceId";
  private static final String B3_SPANID  = "X-B3-SpanId";
  private static final String B3_SAMPLED = "X-B3-Sampled";
  private static final String B3_PARENT  = "X-B3-ParentSpanId";

  @Override public <C> void inject(TraceContext ctx, C c, Setter<C> s) {
    if (ctx==null || !ctx.isValid()) return;
    s.accept(c, Map.entry(B3_TRACEID, ctx.traceId()));
    s.accept(c, Map.entry(B3_SPANID, ctx.spanId()));
    if (ctx.sampled()!=null) s.accept(c, Map.entry(B3_SAMPLED, Boolean.TRUE.equals(ctx.sampled())?"1":"0"));
    if (ctx.parentSpanId()!=null) s.accept(c, Map.entry(B3_PARENT, ctx.parentSpanId()));
    // optional b3 single
    String sampled = ctx.sampled()==null? "" : (Boolean.TRUE.equals(ctx.sampled())?"-1":"-0");
    s.accept(c, Map.entry(B3_SINGLE, ctx.traceId()+"-"+ctx.spanId()+sampled));
  }

  @Override public <C> TraceContext extract(C c, Getter<C> g) {
    String single = g.get(c, B3_SINGLE);
    if (single!=null) {
      var p = single.split("-");
      if (p.length>=2) {
        var sampled = p.length>=3 ? ("1".equals(p[2]) || "d".equalsIgnoreCase(p[2])) : null;
        return new TraceContext(p[0], p[1], sampled, Map.of(), null, Map.of("protocol","b3"));
      }
    }
    String traceId = g.get(c, B3_TRACEID);
    String spanId  = g.get(c, B3_SPANID);
    if (traceId!=null && spanId!=null) {
      String s = g.get(c, B3_SAMPLED);
      Boolean sampled = s==null? null : ("1".equals(s) || "true".equalsIgnoreCase(s));
      return new TraceContext(traceId, spanId, sampled, Map.of(), g.get(c, B3_PARENT), Map.of("protocol","b3"));
    }
    return TraceContext.root();
  }
}
