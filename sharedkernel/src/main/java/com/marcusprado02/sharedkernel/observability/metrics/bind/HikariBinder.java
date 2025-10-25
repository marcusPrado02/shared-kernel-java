package com.marcusprado02.sharedkernel.observability.metrics.bind;

import com.marcusprado02.sharedkernel.observability.metrics.core.*;
import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public final class HikariBinder implements CustomMeterBinder {
    private final String name;
    private final HikariDataSource ds;
    public HikariBinder(String name, HikariDataSource ds){ this.name=name; this.ds=ds; }

    @Override public void bindTo(MetricsFacade m) {
        var base = Map.of("pool", name);
        m.gauge(MetricId.builder("db","hikari.active").unit(Unit.COUNT).tags(base).build(), () -> ds.getHikariPoolMXBean().getActiveConnections(), MeterOptions.builder().build());
        m.gauge(MetricId.builder("db","hikari.idle").unit(Unit.COUNT).tags(base).build(), () -> ds.getHikariPoolMXBean().getIdleConnections(), MeterOptions.builder().build());
        m.gauge(MetricId.builder("db","hikari.total").unit(Unit.COUNT).tags(base).build(), () -> ds.getHikariPoolMXBean().getTotalConnections(), MeterOptions.builder().build());
        m.gauge(MetricId.builder("db","hikari.pending").unit(Unit.COUNT).tags(base).build(), () -> ds.getHikariPoolMXBean().getThreadsAwaitingConnection(), MeterOptions.builder().build());
    }
}