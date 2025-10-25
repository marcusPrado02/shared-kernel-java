package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.util.Map;

import com.marcusprado02.sharedkernel.cqrs.bus.EventStore;

import io.micrometer.core.instrument.MeterRegistry;

public final class EventReplayEngine {
    private final EventStore store;
    private final TimeProvider clock;
    private final MeterRegistry metrics;
    private final int batchSize;

    public EventReplayEngine(EventStore store, TimeProvider clock, MeterRegistry m, int batchSize) {
        this.store = store; this.clock = clock; this.metrics = m; this.batchSize = batchSize;
    }

    public void run(ReplayPlan plan, ReplayCheckpoint checkpoint) throws Exception {
        var cursor = checkpoint.cursorOr(plan.scope().from());
        try (var it = store.read(cursor, batchSize)) {
            while (it.hasNext()) {
                var evt = it.next();
                var up = plan.upcaster().upcast(
                    evt.type(),
                    new String(evt.payload()),                      
                    (Map<String,String>)(Map) evt.headers()
                );
                if (plan.filters().stream().allMatch(f -> f.test(evt))) {
                    for (var t : plan.targets()) t.apply(evt);
                }
                checkpoint.update(evt); 
            }

        }
    }
}

