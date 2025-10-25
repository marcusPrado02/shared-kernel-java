package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.time.Duration;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.command.spi.MetricsFacade;

public final class MetricsMiddleware implements CommandMiddleware {
    private final MetricsFacade metrics;

    public MetricsMiddleware(MetricsFacade metrics){ this.metrics = metrics; }

    @Override
    public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var cmdName = env.command().getClass().getSimpleName();
        var start = System.nanoTime();
        metrics.increment("command_requests_total", "cmd", cmdName, "tenant", safe(env.metadata().tenantId));

        return next.invoke(env).whenComplete((res, err) -> {
            long tookMs = Duration.ofNanos(System.nanoTime() - start).toMillis();
            metrics.timer("command_latency_ms", tookMs, "cmd", cmdName);
            if (err != null) {
                metrics.increment("command_failures_total", "cmd", cmdName, "kind", "exception");
            } else {
                metrics.increment("command_outcomes_total", "cmd", cmdName, "status", res.status().name());
            }
        });
    }

    private static String safe(String v){ return v == null ? "n/a" : v; }
}
