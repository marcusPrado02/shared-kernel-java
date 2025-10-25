package com.marcusprado02.sharedkernel.observability.metrics.bind;

import java.util.*;

import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

public final class CompositeBinder implements CustomMeterBinder {
    private final List<CustomMeterBinder> binders;

    public CompositeBinder(List<CustomMeterBinder> binders) {
        this.binders = List.copyOf(Objects.requireNonNull(binders));
    }

    @Override public void bindTo(MetricsFacade metrics) {
        for (var b : binders) b.bindTo(metrics);
    }
    @Override public void start() { for (var b : binders) b.start(); }
    @Override public void stop()  { for (var b : binders) b.stop(); }
}
