package com.marcusprado02.sharedkernel.observability.tracing;

import java.util.Map;

public interface TracingFacade {
    SpanHandle startSpan(SpanConfig config);

    default SpanHandle startSpan(String name){ 
        return startSpan(SpanConfig.builder(name).build());
    }

    /** Atalhos utilitários */
    default <T> T inSpan(SpanConfig cfg, SpanScopedSupplier<T> supplier) {
        try (var span = startSpan(cfg)) { return supplier.get(span); }
    }
    default void inSpan(SpanConfig cfg, SpanScopedRunnable runnable) {
        try (var span = startSpan(cfg)) { runnable.run(span); }
    }

    interface SpanScopedSupplier<T> { T get(SpanHandle span); }
    interface SpanScopedRunnable { void run(SpanHandle span); }

    String backend(); // "opentelemetry", "brave", etc.
}
