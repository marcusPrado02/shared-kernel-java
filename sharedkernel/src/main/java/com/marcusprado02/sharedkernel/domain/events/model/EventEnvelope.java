package com.marcusprado02.sharedkernel.domain.events.model;

public record EventEnvelope(
        EventMetadata metadata,
        Content payload
) {
    public EventEnvelope with(EventMetadata md, Content p) { return new EventEnvelope(md, p); }
}