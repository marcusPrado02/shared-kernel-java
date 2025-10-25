package com.marcusprado02.sharedkernel.application.denorm;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;

public interface Denormalizer extends ProjectionLike {
    /** Aplica um envelope de evento; deve ser idempotente. */
    void apply(EventEnvelope envelope) throws Exception;
    String name(); // p.ex. "order-denorm-v1"
}