package com.marcusprado02.sharedkernel.observability.tracing;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface TracePropagator {
  <C> void inject(TraceContext ctx, C carrier, Setter<C> setter);
  <C> TraceContext extract(C carrier, Getter<C> getter);

  interface Setter<C> extends BiConsumer<C, Map.Entry<String,String>> {}
  interface Getter<C> {
    String get(C carrier, String key);
    Iterable<String> keys(C carrier);
  }
}
