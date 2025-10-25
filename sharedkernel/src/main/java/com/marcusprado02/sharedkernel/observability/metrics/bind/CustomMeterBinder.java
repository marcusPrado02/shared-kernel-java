package com.marcusprado02.sharedkernel.observability.metrics.bind;

import com.marcusprado02.sharedkernel.observability.metrics.facade.MetricsFacade;

/** Semelhante a MeterBinder do Micrometer, mas backend-agnostic. */
public interface CustomMeterBinder {
    /** Registra as métricas no MetricsFacade. Idempotente. */
    void bindTo(MetricsFacade metrics);

    /** Ciclo de vida opcional p/ binders com threads/schedulers. */
    default void start() {}
    default void stop() {}
}
