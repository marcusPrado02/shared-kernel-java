package com.marcusprado02.sharedkernel.observability.tracing.adapters.kafka;

import com.marcusprado02.sharedkernel.observability.tracing.*;
import org.apache.kafka.common.header.Headers;

public final class KafkaHeadersCarrier {
  public static TracePropagator.Getter<Headers> getter() {
    return new TracePropagator.Getter<Headers>() {
      @Override public String get(Headers h, String k){
        var i = h.lastHeader(k); return i==null? null : new String(i.value(), java.nio.charset.StandardCharsets.UTF_8);
      }
      @Override public Iterable<String> keys(Headers h){
        var set = new java.util.LinkedHashSet<String>();
        h.forEach(e -> set.add(e.key())); return set;
      }
    };
  }
  public static TracePropagator.Setter<Headers> setter() {
    return (h, kv) -> {
      h.remove(kv.getKey());
      h.add(kv.getKey(), kv.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    };
  }
}
