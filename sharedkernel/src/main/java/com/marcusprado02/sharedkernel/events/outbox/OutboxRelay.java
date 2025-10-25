package com.marcusprado02.sharedkernel.events.outbox;


import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;

import com.marcusprado02.sharedkernel.cqrs.command.spi.ClockProvider;
import com.marcusprado02.sharedkernel.cqrs.command.spi.MetricsFacade;
import com.marcusprado02.sharedkernel.events.spi.MessageBroker;

public final class OutboxRelay {
    private final OutboxRepository repo;
    private final MessageBroker broker;
    private final MetricsFacade metrics;
    private final ClockProvider clock;
    private final int batchSize;

    public OutboxRelay(OutboxRepository repo, MessageBroker broker, MetricsFacade metrics, ClockProvider clock, int batchSize) {
        this.repo=repo; this.broker=broker; this.metrics=metrics; this.clock=clock; this.batchSize=batchSize;
    }

    public void start() {
        Executors.newSingleThreadExecutor().submit(this::loop);
    }

    private void loop() {
        while (true) {
            var now = clock.nowUtc();
            var batch = repo.fetchBatch(batchSize, now);
            if (batch.isEmpty()) { sleep(100); continue; }

            for (var rec : batch) {
                try {
                    broker.publish(rec.eventType, rec.id, rec.payloadJson, rec.tenantId, rec.traceparent);
                    repo.markSent(rec.id);
                    metrics.increment("outbox_sent", "type", rec.eventType);
                } catch (Exception ex) {
                    var next = now.plus(Duration.ofSeconds(backoff(rec.attempts)));
                    repo.markFailed(rec.id, ex.getMessage(), next);
                    metrics.increment("outbox_failed", "type", rec.eventType);
                }
            }
        }
    }

    private long backoff(int attempts){ return Math.min(300, (long)Math.pow(2, Math.max(0, attempts))) ; }
    private static void sleep(long ms){ try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
