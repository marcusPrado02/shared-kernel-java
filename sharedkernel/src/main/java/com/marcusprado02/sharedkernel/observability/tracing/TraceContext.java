package com.marcusprado02.sharedkernel.observability.tracing;


import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public record TraceContext(
    String traceId, String spanId, Boolean sampled,
    Map<String,String> baggage, String parentSpanId, Map<String,String> attributes
) {
  public static TraceContext root() {
    return new TraceContext(null,null,null, Map.of(), null, Map.of());
  }
  public TraceContext withAttr(String k, String v) { 
    var m = new java.util.HashMap<>(attributes); m.put(k,v); 
    return new TraceContext(traceId, spanId, sampled, baggage, parentSpanId, m);
  }
  public boolean isValid(){ return traceId!=null && spanId!=null; }
}
