package com.marcusprado02.sharedkernel.observability.metrics.bind;

import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

import java.util.Map;

public final class ThreadPoolBinder implements CustomMeterBinder {
    private final String name;
    private final ThreadPoolExecutor exec;

    public ThreadPoolBinder(String name, ThreadPoolExecutor exec){
        this.name = name;
        this.exec = exec;
    }

    @Override public void bindTo(MetricsFacade m) {
        var base = Map.of("pool", name);
        m.gauge(MetricId.builder("executor","queue.size").unit(Unit.COUNT).tags(base).build(), () -> exec.getQueue().size(), MeterOptions.builder().build());
        m.gauge(MetricId.builder("executor","active").unit(Unit.COUNT).tags(base).build(), exec::getActiveCount, MeterOptions.builder().build());
        m.gauge(MetricId.builder("executor","pool.size").unit(Unit.COUNT).tags(base).build(), exec::getPoolSize, MeterOptions.builder().build());
        m.gauge(MetricId.builder("executor","completed.tasks").unit(Unit.COUNT).tags(base).build(), () -> (double) exec.getCompletedTaskCount(), MeterOptions.builder().build());
    }
}
