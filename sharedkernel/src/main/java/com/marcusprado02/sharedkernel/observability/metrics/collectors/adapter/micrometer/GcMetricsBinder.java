package com.marcusprado02.sharedkernel.observability.metrics.collectors.adapter.micrometer;

import com.marcusprado02.sharedkernel.observability.metrics.collectors.GarbageCollectorMonitor;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;

public final class GcMetricsBinder implements MeterBinder, AutoCloseable {
    private final GarbageCollectorMonitor monitor;
    private Gauge p50, p95, p99, gcRatio, allocBps, promoBps, oldUtil, eden, surv, old, meta;
    public GcMetricsBinder(GarbageCollectorMonitor m){ this.monitor = m; }
    @Override public void bindTo(MeterRegistry r) {
        p50 = Gauge.builder("gc.pause.p50.ms", () -> monitor.current().pauseP50ms()).register(r);
        p95 = Gauge.builder("gc.pause.p95.ms", () -> monitor.current().pauseP95ms()).register(r);
        p99 = Gauge.builder("gc.pause.p99.ms", () -> monitor.current().pauseP99ms()).register(r);
        gcRatio = Gauge.builder("gc.time.ratio.pct", () -> monitor.current().gcTimeRatioPct()).register(r);
        allocBps = Gauge.builder("gc.alloc.bps", () -> monitor.current().allocationBytesPerSec()).baseUnit("bytes").register(r);
        promoBps = Gauge.builder("gc.promotion.bps", () -> monitor.current().promotionBytesPerSec()).baseUnit("bytes").register(r);
        oldUtil  = Gauge.builder("gc.old.utilization.pct", () -> monitor.current().oldUtilizationPct()).register(r);
        eden     = Gauge.builder("gc.pool.eden.used.bytes", () -> monitor.current().edenUsed()).baseUnit("bytes").register(r);
        surv     = Gauge.builder("gc.pool.survivor.used.bytes", () -> monitor.current().survivorUsed()).baseUnit("bytes").register(r);
        old      = Gauge.builder("gc.pool.old.used.bytes", () -> monitor.current().oldUsed()).baseUnit("bytes").register(r);
        meta     = Gauge.builder("gc.pool.metaspace.used.bytes", () -> monitor.current().metaspaceUsed()).baseUnit("bytes").register(r);
    }
    @Override public void close() { /* nada */ }
}