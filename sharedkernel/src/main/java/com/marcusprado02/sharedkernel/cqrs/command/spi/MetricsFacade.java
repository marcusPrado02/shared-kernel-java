package com.marcusprado02.sharedkernel.cqrs.command.spi;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public interface MetricsFacade {
    void increment(String counter, String... tags);
    void timer(String name, long durationMs, String... tags);

    /** Implementação Micrometer */
    static MetricsFacade micrometer(MeterRegistry reg){
        return new MetricsFacade(){
            @Override public void increment(String counter, String... tags){
                Counter.builder(counter)
                    .tags(tags == null ? new String[0] : tags)
                    .register(reg).increment();
            }
            @Override public void timer(String name, long ms, String... tags){
                Timer.builder(name)
                    .tags(tags == null ? new String[0] : tags)
                    .register(reg).record(java.time.Duration.ofMillis(ms));
            }
        };
    }
}
