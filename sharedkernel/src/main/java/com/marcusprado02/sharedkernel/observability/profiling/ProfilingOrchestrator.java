package com.marcusprado02.sharedkernel.observability.profiling;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProfilingOrchestrator implements AutoCloseable {
    private final ScheduledExecutorService ses;
    private final List<ProfilingTrigger> triggers;
    private final ProfilerAdapter profiler;
    private final Duration pollInterval;
    private final Duration profilingDuration;
    private final List<TriggerListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean profilingActive = false;

    public ProfilingOrchestrator(List<ProfilingTrigger> triggers,
                                 ProfilerAdapter profiler,
                                 Duration pollInterval,
                                 Duration profilingDuration) {
        this.triggers = List.copyOf(triggers);
        this.profiler = profiler;
        this.pollInterval = pollInterval;
        this.profilingDuration = profilingDuration;
        this.ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "profiling-orchestrator"); t.setDaemon(true); return t;
        });
    }

    public void start(Supplier<ProfilingContext> contextSupplier) {
        ses.scheduleAtFixedRate(() -> {
            try {
                var ctx = contextSupplier.get();
                for (var t : triggers) {
                    var res = t.evaluate(ctx);
                    for (var l : listeners) l.onDecision(t.name(), res, ctx);
                    if (res.decision()==Decision.TRIGGER) {
                        maybeProfileOnce(res, ctx);
                    }
                }
            } catch (Throwable e) {
                // nunca deixar o scheduler morrer
                e.printStackTrace();
            }
        }, 0, pollInterval.toMillis(), TimeUnit.MILLISECONDS);
    }

    private synchronized void maybeProfileOnce(EvaluationResult res, ProfilingContext ctx) {
        if (profilingActive) return;
        profilingActive = true;
        profiler.start(ctx, res);
        ses.schedule(() -> {
            try {
                profiler.stop(ctx, res);
            } finally {
                profilingActive = false;
            }
        }, profilingDuration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void addListener(TriggerListener l){ listeners.add(l); }
    @Override public void close() { ses.shutdownNow(); }
}