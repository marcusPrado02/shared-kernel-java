package com.marcusprado02.sharedkernel.domain.events.upcast;

import com.marcusprado02.sharedkernel.domain.events.model.EventEnvelope;
import com.marcusprado02.sharedkernel.domain.events.model.EventType;

public interface EventUpcaster {
    /** Tipo de evento que este upcaster atende. */
    EventType eventType();

    /** Versão de origem que este upcaster migra (ex.: 1 → 2). */
    int fromVersion();

    /** Versão de destino após a migração. */
    int toVersion();

    /** Aplica a transformação determinística. */
    UpcastResult apply(EventEnvelope in, UpcastContext ctx);
}
