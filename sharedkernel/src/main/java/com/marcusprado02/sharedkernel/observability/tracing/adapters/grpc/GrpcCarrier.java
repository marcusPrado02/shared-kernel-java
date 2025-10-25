package com.marcusprado02.sharedkernel.observability.tracing.adapters.grpc;

import com.marcusprado02.sharedkernel.observability.tracing.*;

public final class GrpcCarrier {
  public static final io.grpc.Metadata.Key<String> ANY = io.grpc.Metadata.Key.of("x-any", io.grpc.Metadata.ASCII_STRING_MARSHALLER);

  public static TracePropagator.Getter<io.grpc.Metadata> getter() {
    return new TracePropagator.Getter<>() {
      @Override public String get(io.grpc.Metadata m, String k){ return m.get(io.grpc.Metadata.Key.of(k, io.grpc.Metadata.ASCII_STRING_MARSHALLER)); }
      @Override public Iterable<String> keys(io.grpc.Metadata m){ return m.keys(); }
    };
  }
  public static TracePropagator.Setter<io.grpc.Metadata> setter() {
    return (m, kv) -> m.put(io.grpc.Metadata.Key.of(kv.getKey(), io.grpc.Metadata.ASCII_STRING_MARSHALLER), kv.getValue());
  }
}
