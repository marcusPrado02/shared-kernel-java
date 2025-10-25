package com.marcusprado02.sharedkernel.crosscutting.generators.telemetry;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class TelemetryGenerator<T> implements Generator<T> {
    private final Generator<T> delegate; private final Telemetry telemetry; private final String metricPrefix;
    public TelemetryGenerator(Generator<T> d, Telemetry t, String prefix){
        this.delegate=d; this.telemetry=t; this.metricPrefix=prefix;
    }
    @Override public T generate(GenerationContext ctx) {
        try {
            return telemetry.time(metricPrefix + ".latency", () -> {
                T v = delegate.generate(ctx);
                telemetry.count(metricPrefix + ".success", 1);
                return v;
            });
        } catch (RuntimeException re) { telemetry.count(metricPrefix + ".error", 1); throw re;
        } catch (Exception e) { telemetry.count(metricPrefix + ".error", 1); throw new GenerationException("obs failed", e); }
    }
}