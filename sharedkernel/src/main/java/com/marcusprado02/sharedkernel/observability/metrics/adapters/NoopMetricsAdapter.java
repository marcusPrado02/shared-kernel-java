package com.marcusprado02.sharedkernel.observability.metrics.adapters;


import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.metrics.spi.MetricsAdapter;

public final class NoopMetricsAdapter implements MetricsAdapter {
    private static final MetricsFacade.Counter NOOP_COUNTER = new MetricsFacade.Counter(){ public void inc(){} public void add(double a){} };
    private static final MetricsFacade.Timer NOOP_TIMER = new MetricsFacade.Timer(){ public void record(long a, TimeUnit u){} };
    private static final MetricsFacade.Distribution NOOP_DIST = new MetricsFacade.Distribution(){ public void observe(double a){} };
    private static final MetricsFacade.Gauge NOOP_GAUGE = new MetricsFacade.Gauge(){};

    @Override public MetricsFacade.Counter newCounter(MetricId id, MeterOptions options){ return NOOP_COUNTER; }
    @Override public MetricsFacade.Timer newTimer(MetricId id, MeterOptions options){ return NOOP_TIMER; }
    @Override public MetricsFacade.Distribution newDistribution(MetricId id, MeterOptions options){ return NOOP_DIST; }
    @Override public MetricsFacade.Gauge newGauge(MetricId id, DoubleSupplier s, MeterOptions options){ return NOOP_GAUGE; }

    @Override public void increment(MetricId id, double amount, Map<String,String> tags) {}
    @Override public void recordTime(MetricId id, long amount, TimeUnit unit, Map<String,String> tags) {}
    @Override public void observe(MetricId id, double amount, Map<String,String> tags) {}
    @Override public void gaugeOneShot(MetricId id, double value, Map<String,String> tags) {}
    @Override public String backendName(){ return "noop"; }
}
