package com.marcusprado02.sharedkernel.application.denorm;

import com.marcusprado02.sharedkernel.application.denorm.store.DeadLetterStore;
import com.marcusprado02.sharedkernel.application.denorm.store.OffsetStore;
import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public abstract class BaseDenormalizer implements Denormalizer {
    protected final OffsetStore offsets;
    protected final DeadLetterStore dlq;

    protected BaseDenormalizer(OffsetStore offsets, DeadLetterStore dlq) {
        this.offsets = offsets; this.dlq = dlq;
    }

    @Override public void apply(EventEnvelope env) throws Exception {
        if (offsets.wasProcessed(name(), env.metadata().eventId())) return;
        try {
            route(env); // sua lógica de regras
            offsets.markProcessed(name(), env.metadata().eventId(), env.metadata().sequence());
        } catch (Exception ex) {
            dlq.put(name(), env.metadata().eventId(), env.metadata().eventType().toString(), ex.getMessage(), toJson(env.payload()));
            throw ex;
        }
    }

    protected abstract void route(EventEnvelope env) throws Exception;

    protected String toJson(Object o) { /* usar Jackson */ return "{}"; }
}
