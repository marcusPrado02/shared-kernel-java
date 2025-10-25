package com.marcusprado02.sharedkernel.infrastructure.idempotency;

import jakarta.persistence.Id;

import java.time.OffsetDateTime;

public class ProjectionOffset {
    @Id
    private String projectionName;
    private long lastSequence;
    private String lastEventId;
    private OffsetDateTime updatedAt;
}
