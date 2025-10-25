package com.marcusprado02.sharedkernel.observability.metrics.impl;


import java.util.Map;
import java.util.Objects;
import java.util.function.DoubleSupplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.metrics.spi.MetricsAdapter;

public final class MetricsFacadeImpl implements MetricsFacade {
    private final MetricsAdapter adapter;
    public MetricsFacadeImpl(MetricsAdapter adapter) {
        this.adapter = Objects.requireNonNull(adapter);
    }
    @Override public Counter counter(MetricId id, MeterOptions options){ return adapter.newCounter(id, options); }
    @Override public Timer timer(MetricId id, MeterOptions options){ return adapter.newTimer(id, options); }
    @Override public Distribution distribution(MetricId id, MeterOptions options){ return adapter.newDistribution(id, options); }
    @Override public Gauge gauge(MetricId id, DoubleSupplier s, MeterOptions options){ return adapter.newGauge(id, s, options); }
    @Override public void increment(MetricId id, double amount, Map<String,String> tags){ adapter.increment(id, amount, tags); }
    @Override public void recordTime(MetricId id, long amount, java.util.concurrent.TimeUnit u, Map<String,String> tags){ adapter.recordTime(id, amount, u, tags); }
    @Override public void observe(MetricId id, double amount, Map<String,String> tags){ adapter.observe(id, amount, tags); }
    @Override public void gaugeOneShot(MetricId id, double value, Map<String,String> tags){ adapter.gaugeOneShot(id, value, tags); }
    @Override public String backend(){ return adapter.backendName(); }
}
