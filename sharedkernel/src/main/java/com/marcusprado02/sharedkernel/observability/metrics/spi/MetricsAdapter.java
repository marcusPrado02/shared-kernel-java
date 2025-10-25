package com.marcusprado02.sharedkernel.observability.metrics.spi;


import java.util.Map;
import java.util.function.DoubleSupplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

public interface MetricsAdapter {
    MetricsFacade.Counter newCounter(MetricId id, MeterOptions options);
    MetricsFacade.Timer newTimer(MetricId id, MeterOptions options);
    MetricsFacade.Distribution newDistribution(MetricId id, MeterOptions options);
    MetricsFacade.Gauge newGauge(MetricId id, DoubleSupplier supplier, MeterOptions options);

    void increment(MetricId id, double amount, Map<String,String> tags);
    void recordTime(MetricId id, long amount, java.util.concurrent.TimeUnit unit, Map<String,String> tags);
    void observe(MetricId id, double amount, Map<String,String> tags);
    void gaugeOneShot(MetricId id, double value, Map<String,String> tags);

    String backendName();
}