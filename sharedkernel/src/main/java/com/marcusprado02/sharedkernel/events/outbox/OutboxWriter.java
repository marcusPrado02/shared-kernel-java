package com.marcusprado02.sharedkernel.events.outbox;

import java.time.Instant;
import java.util.UUID;

import com.marcusprado02.sharedkernel.cqrs.command.spi.ClockProvider;
import com.marcusprado02.sharedkernel.events.integration.IntegrationEvent;
import com.marcusprado02.sharedkernel.events.integration.IntegrationEventPublisher;
import com.marcusprado02.sharedkernel.events.spi.JsonSerializer;

public final class OutboxWriter implements IntegrationEventPublisher {
    private final OutboxRepository repo;
    private final JsonSerializer json;
    private final ClockProvider clock;

    public OutboxWriter(OutboxRepository repo, JsonSerializer json, ClockProvider clock){
        this.repo=repo; this.json=json; this.clock=clock;
    }

    @Override public void publish(IntegrationEvent ev) {
        var rec = new OutboxRecord();
        rec.id = newKsuid(); // ou ULID monotônico
        rec.eventType = ev.eventType();
        rec.schemaVersion = ev.schemaVersion();
        rec.aggregateId = ev.aggregateId();
        rec.occurredAt = ev.occurredAt();
        rec.tenantId = (String) ev.payload().getOrDefault("_tenant", "default");
        rec.payloadJson = json.toJson(ev.payload());
        rec.status = "NEW";
        rec.attempts = 0;
        rec.nextAttemptAt = clock.nowUtc();
        rec.traceparent = (String) ev.payload().getOrDefault("_trace", null);
        rec.correlationId = (String) ev.payload().getOrDefault("_corr", null);
        rec.causationId = (String) ev.payload().getOrDefault("_caus", null);
        repo.save(rec); // dentro da mesma transação do aggregate
    }

    private static String newKsuid(){ return UUID.randomUUID().toString().replace("-",""); }
}
