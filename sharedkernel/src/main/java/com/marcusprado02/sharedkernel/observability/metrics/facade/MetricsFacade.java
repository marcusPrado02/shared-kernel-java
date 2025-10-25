package com.marcusprado02.sharedkernel.observability.metrics.facade;


import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;

public interface MetricsFacade {

    // Handles de uso rápido (mantêm config e tags base).
    interface Counter {
        void inc();
        void add(double amount);
    }
    interface Timer {
        void record(long amount, TimeUnit unit);
        default Scope time() { return new Scope(this); }
        final class Scope implements AutoCloseable {
            private final Timer t;
            private final long start = System.nanoTime();
            Scope(Timer t){ this.t=t; }
            @Override public void close(){ t.record(System.nanoTime()-start, TimeUnit.NANOSECONDS); }
        }
    }
    interface Distribution {
        void observe(double amount);
    }
    interface Gauge {
        // Gauge registrável via supplier; não possui 'set' para evitar statefulness
    }

    // Fábrica de handles (com opções finas)
    Counter counter(MetricId id, MeterOptions options);
    Timer timer(MetricId id, MeterOptions options);
    Distribution distribution(MetricId id, MeterOptions options);
    Gauge gauge(MetricId id, DoubleSupplier supplier, MeterOptions options);

    // Atalhos com defaults (sem options)
    default Counter counter(MetricId id){ return counter(id, MeterOptions.builder().build()); }
    default Timer timer(MetricId id){ return timer(id, MeterOptions.builder().build()); }
    default Distribution distribution(MetricId id){ return distribution(id, MeterOptions.builder().build()); }
    default Gauge gauge(MetricId id, DoubleSupplier s){ return gauge(id, s, MeterOptions.builder().build()); }

    // Operações stateless rápidas (quando não vale manter handle)
    void increment(MetricId id, double amount, Map<String,String> tags);
    void recordTime(MetricId id, long amount, TimeUnit unit, Map<String,String> tags);
    void observe(MetricId id, double amount, Map<String,String> tags);
    void gaugeOneShot(MetricId id, double value, Map<String,String> tags);

    // Utilidade (para saber backend, health, etc.)
    String backend();
}
