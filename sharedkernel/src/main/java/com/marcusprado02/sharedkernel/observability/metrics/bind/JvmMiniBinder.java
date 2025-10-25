package com.marcusprado02.sharedkernel.observability.metrics.bind;

import java.lang.management.*;
import java.util.Map;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

public final class JvmMiniBinder implements CustomMeterBinder {
    private final MetricId heapUsed = MetricId.builder("jvm","memory.heap.used").unit(Unit.BYTES).build();
    private final MetricId heapCommitted = MetricId.builder("jvm","memory.heap.committed").unit(Unit.BYTES).build();
    private final MetricId threadsLive = MetricId.builder("jvm","threads.live").unit(Unit.COUNT).build();

    @Override public void bindTo(MetricsFacade m) {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        ThreadMXBean th = ManagementFactory.getThreadMXBean();

        m.gauge(heapUsed, () -> mem.getHeapMemoryUsage().getUsed(), MeterOptions.builder().build());
        m.gauge(heapCommitted, () -> mem.getHeapMemoryUsage().getCommitted(), MeterOptions.builder().build());
        m.gauge(threadsLive, th::getThreadCount, MeterOptions.builder().build());
        // counters/tempos de GC poderiam ser adicionados via com.sun... se desejado (dependência de VM)
        m.increment(MetricId.builder("jvm","info").unit(Unit.COUNT).build(), 1, Map.of("vendor", System.getProperty("java.vendor"), "version", System.getProperty("java.version")));
    }
}
