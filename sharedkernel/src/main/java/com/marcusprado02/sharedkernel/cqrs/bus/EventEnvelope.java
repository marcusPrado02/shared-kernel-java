package com.marcusprado02.sharedkernel.cqrs.bus;

import java.time.OffsetDateTime;
import java.util.Map;

public record EventEnvelope(
    String id, String stream, long sequence, String type,
    OffsetDateTime occurredAt, Map<String,Object> headers, byte[] payload, int schemaVersion
) {}
