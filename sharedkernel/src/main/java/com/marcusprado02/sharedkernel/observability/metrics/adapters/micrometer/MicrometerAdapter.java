package com.marcusprado02.sharedkernel.observability.metrics.adapters.micrometer;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.marcusprado02.sharedkernel.observability.metrics.spi.MetricsAdapter;

public final class MicrometerAdapter implements MetricsAdapter {
    private final MeterRegistry registry;
    private final NameSanitizer sanitizer;
    private final CardinalityLimiter limiter;

    public MicrometerAdapter(MeterRegistry registry, NameSanitizer sanitizer, CardinalityLimiter limiter) {
        this.registry = Objects.requireNonNull(registry);
        this.sanitizer = Objects.requireNonNull(sanitizer);
        this.limiter = Objects.requireNonNull(limiter);
    }

    @Override public MetricsFacade.Counter newCounter(MetricId id, MeterOptions opt) {
        Counter counter = Counter.builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, opt)))
                .register(registry);
        return new MetricsFacade.Counter() {
            @Override public void inc() { counter.increment(); }
            @Override public void add(double amount) { counter.increment(amount); }
        };
    }

    @Override public MetricsFacade.Timer newTimer(MetricId id, MeterOptions opt) {
        var b = Timer.builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, opt)))
                .distributionStatisticExpiry(opt.expiry())
                .publishPercentiles(opt.percentiles());
        if (opt.percentileHistogram()) b.publishPercentileHistogram();
        if (opt.slaBoundaries().length > 0) b.serviceLevelObjectives(toDurations(opt.slaBoundaries()));
        Timer t = b.register(registry);
        return (amount, unit) -> t.record(amount, unit);
    }

    @Override public MetricsFacade.Distribution newDistribution(MetricId id, MeterOptions opt) {
        DistributionSummary.Builder b = DistributionSummary.builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, opt)))
                .distributionStatisticExpiry(opt.expiry())
                .publishPercentiles(opt.percentiles());
        if (opt.percentileHistogram()) b.publishPercentileHistogram();
        if (opt.slaBoundaries().length > 0) b.serviceLevelObjectives(opt.slaBoundaries());
        DistributionSummary ds = b.register(registry);
        return ds::record;
    }

    @Override public MetricsFacade.Gauge newGauge(MetricId id, DoubleSupplier supplier, MeterOptions opt) {
        Gauge.builder(sanitizer.metricName(id.fqName()), supplier, DoubleSupplier::getAsDouble)
                .description(id.description())
                .tags(toTags(mergeTags(id, opt)))
                .register(registry);
        return new MetricsFacade.Gauge(){};
    }

    @Override public void increment(MetricId id, double amount, Map<String,String> tags) {
        var c = io.micrometer.core.instrument.Counter
                .builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, tags)))
                .register(registry);
        c.increment(amount);
    }

    @Override public void recordTime(MetricId id, long amount, TimeUnit unit, Map<String,String> tags) {
        var t = Timer.builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, tags)))
                .register(registry);
        t.record(amount, unit);
    }

    @Override public void observe(MetricId id, double amount, Map<String,String> tags) {
        var d = DistributionSummary.builder(sanitizer.metricName(id.fqName()))
                .description(id.description())
                .tags(toTags(mergeTags(id, tags)))
                .register(registry);
        d.record(amount);
    }

    @Override public void gaugeOneShot(MetricId id, double value, Map<String,String> tags) {
        // Em Micrometer não há one-shot nativo; registramos um gauge-volatile e removemos? Mantemos simples:
        Gauge.builder(sanitizer.metricName(id.fqName()), () -> value)
                .description(id.description())
                .tags(toTags(mergeTags(id, tags)))
                .register(registry);
    }

    @Override public String backendName(){ return "micrometer"; }

    /* helpers */
    private Iterable<Tag> toTags(Map<String,String> map) {
        if (map == null || map.isEmpty()) return List.of();
        List<Tag> res = new ArrayList<>(map.size());
        for (var e : limiter.limit(map).entrySet()) {
            res.add(Tag.of(sanitizer.tagKey(e.getKey()), sanitizer.tagValue(e.getValue())));
        }
        return res;
    }
    private Map<String,String> mergeTags(MetricId id, MeterOptions opt){
        Map<String,String> m = new LinkedHashMap<>();
        if (id.baseTags()!=null) m.putAll(id.baseTags());
        if (opt!=null && opt.extraTags()!=null) m.putAll(opt.extraTags());
        return m;
    }
    private Map<String,String> mergeTags(MetricId id, Map<String,String> more){
        Map<String,String> m = new LinkedHashMap<>();
        if (id.baseTags()!=null) m.putAll(id.baseTags());
        if (more!=null) m.putAll(more);
        return m;
    }
    private Duration[] toDurations(double[] millis) {
        Duration[] ds = new Duration[millis.length];
        for (int i=0;i<millis.length;i++) ds[i] = Duration.ofMillis((long)millis[i]);
        return ds;
    }
}
