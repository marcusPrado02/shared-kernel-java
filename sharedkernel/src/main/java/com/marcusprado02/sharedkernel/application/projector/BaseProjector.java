package com.marcusprado02.sharedkernel.application.projector;

import org.springframework.transaction.annotation.Transactional;

public abstract class BaseProjector<T> implements Projector<T> {

    protected abstract boolean wasProcessed(String eventId);
    protected abstract void markProcessed(String eventId, long sequence);
    protected abstract void handle(T event) throws Exception;

    @Override @Transactional
    public void apply(T event) throws Exception {
        var meta = ProjectionMetadata.extract(event); // pega eventId, seq, timestamp
        if (wasProcessed(meta.id())) return;
        handle(event);
        markProcessed(meta.id(), meta.sequence());
    }
}
