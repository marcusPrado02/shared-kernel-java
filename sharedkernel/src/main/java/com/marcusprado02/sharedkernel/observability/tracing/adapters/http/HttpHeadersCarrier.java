package com.marcusprado02.sharedkernel.observability.tracing.adapters.http;

import java.util.Collections;

import com.marcusprado02.sharedkernel.observability.tracing.TracePropagator;

// Getter/Setter para Map-like headers (Servlet, Spring Web, JAX-RS)
public final class HttpHeadersCarrier {
  public static TracePropagator.Getter<jakarta.servlet.http.HttpServletRequest> requestGetter() {
    return new TracePropagator.Getter<>() {
      @Override public String get(jakarta.servlet.http.HttpServletRequest r, String k){ return r.getHeader(k); }
      @Override public Iterable<String> keys(jakarta.servlet.http.HttpServletRequest r){ return Collections.list(r.getHeaderNames()); }
    };
  }
  public static TracePropagator.Setter<org.springframework.http.HttpHeaders> springSetter() {
    return (hdrs, kv) -> hdrs.set(kv.getKey(), kv.getValue());
  }
}