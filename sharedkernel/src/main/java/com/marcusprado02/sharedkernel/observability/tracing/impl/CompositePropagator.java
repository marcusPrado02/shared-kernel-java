package com.marcusprado02.sharedkernel.observability.tracing.impl;


import java.util.*;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class CompositePropagator implements TracePropagator {
  private final List<TracePropagator> chain;

  public CompositePropagator(TracePropagator... propagators) {
    this.chain = List.of(propagators);
  }
  @Override public <C> void inject(TraceContext ctx, C carrier, Setter<C> s) {
    for (var p : chain) p.inject(ctx, carrier, s);
  }
  @Override public <C> TraceContext extract(C carrier, Getter<C> g) {
    for (var p : chain) {
      var c = p.extract(carrier, g);
      if (c!=null && c.isValid()) return c;
    }
    return TraceContext.root(); // noop
  }
}
