package com.marcusprado02.sharedkernel.infrastructure.sms.model;

import java.time.Instant;
import java.util.Map;

public record SmsResponse(
        String messageId, SmsStatus status, SmsErrorCode error,
        String idempotencyKey, String to, String body,
        String providerId, Map<String, Object> raw, Instant acceptedAt
) {}
