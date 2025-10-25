package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.Map;

public record EmailResponse(
        String messageId, EmailStatus status, EmailErrorCode error,
        String providerId, Map<String,Object> raw, java.time.Instant acceptedAt
) {}
