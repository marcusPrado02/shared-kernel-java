package com.marcusprado02.sharedkernel.observability.metrics.bind;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.DoubleSupplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

public final class ScheduledPollBinder implements CustomMeterBinder, AutoCloseable {
    public static final class GaugeDef {
        public final MetricId id;
        public final DoubleSupplier supplier;
        public final MeterOptions options;
        GaugeDef(MetricId id, DoubleSupplier s, MeterOptions o){ this.id=id; this.supplier=s; this.options=o; }
    }

    private final List<GaugeDef> gauges = new ArrayList<>();
    private final Duration period;
    private ScheduledExecutorService ses;
    private volatile MetricsFacade metrics;
    private final TagEnricher enricher;

    public ScheduledPollBinder(Duration period, TagEnricher enricher) {
        this.period = Objects.requireNonNull(period);
        this.enricher = enricher==null? TagEnricher.noop(): enricher;
    }

    public ScheduledPollBinder gauge(MetricId id, DoubleSupplier supplier, MeterOptions options){
        gauges.add(new GaugeDef(id, supplier, options==null? MeterOptions.builder().build(): options));
        return this;
    }

    @Override public void bindTo(MetricsFacade metrics) {
        this.metrics = metrics;
        // primeiro push imediato (evita “buraco” no dashboard até o primeiro tick)
        for (var g : gauges) {
            metrics.gauge(g.id, g.supplier, g.options);  // registro “permanente”
            metrics.gaugeOneShot(g.id, g.supplier.getAsDouble(), enricher.enrich(Map.of()));
        }
    }

    @Override public void start() {
        if (ses != null) return;
        ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "metrics-poll-binder");
            t.setDaemon(true);
            return t;
        });
        ses.scheduleAtFixedRate(() -> {
            if (metrics == null) return;
            for (var g : gauges) {
                metrics.gaugeOneShot(g.id, g.supplier.getAsDouble(), enricher.enrich(Map.of()));
            }
        }, period.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override public void stop() { close(); }
    @Override public void close() {
        if (ses != null) {
            ses.shutdownNow();
            ses = null;
        }
    }
}