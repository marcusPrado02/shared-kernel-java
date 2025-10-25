package com.marcusprado02.sharedkernel.observability.tracing.impl;


import java.util.*;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class W3CTraceContextPropagator implements TracePropagator {
  private static final String TRACEPARENT = "traceparent";
  private static final String TRACESTATE  = "tracestate";
  private static final String BAGGAGE     = "baggage";

  @Override public <C> void inject(TraceContext ctx, C carrier, Setter<C> s) {
    if (ctx==null || !ctx.isValid()) return;
    // versão 00: traceparent = 00-<traceId>-<spanId>-<flags>
    String flags = Boolean.TRUE.equals(ctx.sampled()) ? "01" : "00";
    String tp = String.format("00-%s-%s-%s", ctx.traceId(), ctx.spanId(), flags);
    s.accept(carrier, Map.entry(TRACEPARENT, tp));
    // opcional: tracestate (vendor=kv)
    // baggage
    if (ctx.baggage()!=null && !ctx.baggage().isEmpty()) {
      String bag = ctx.baggage().entrySet().stream()
          .map(e -> e.getKey() + "=" + percentEncode(e.getValue()))
          .reduce((a,b)->a+","+b).orElse(null);
      if (bag!=null) s.accept(carrier, Map.entry(BAGGAGE, bag));
    }
  }

  @Override public <C> TraceContext extract(C carrier, Getter<C> g) {
    String tp = g.get(carrier, TRACEPARENT);
    if (tp==null || tp.length()<55) return TraceContext.root();
    try {
      // 00-<32hex>-<16hex>-<2hex>
      var p = tp.split("-");
      String traceId = p[1], spanId = p[2], flags = p[3];
      Boolean sampled = (Integer.parseInt(flags,16) & 0x01) == 1;
      Map<String,String> baggage = parseBaggage(g.get(carrier, BAGGAGE));
      return new TraceContext(traceId, spanId, sampled, baggage, null, Map.of("protocol","w3c"));
    } catch (Exception ignored) {
      return TraceContext.root();
    }
  }

  private static Map<String,String> parseBaggage(String v){
    if (v==null || v.isBlank()) return Map.of();
    var map = new LinkedHashMap<String,String>();
    for (var part : v.split(",")) {
      var kv = part.trim().split("=",2);
      if (kv.length==2) map.put(kv[0].trim(), percentDecode(kv[1].trim()));
    }
    return map;
  }
  private static String percentEncode(String v){ return java.net.URLEncoder.encode(v, java.nio.charset.StandardCharsets.UTF_8); }
  private static String percentDecode(String v){ return java.net.URLDecoder.decode(v, java.nio.charset.StandardCharsets.UTF_8); }
}

