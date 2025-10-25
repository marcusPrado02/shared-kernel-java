package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.Map;

public record EmailEvent(
        String providerId, String messageId, EmailStatus status, String eventType,
        String reason, java.time.Instant timestamp, Map<String,Object> raw
) {}
