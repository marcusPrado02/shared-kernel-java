package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record WebhookEvent(
        String providerId, String eventType, String objectId, Map<String,Object> payload
) {}
