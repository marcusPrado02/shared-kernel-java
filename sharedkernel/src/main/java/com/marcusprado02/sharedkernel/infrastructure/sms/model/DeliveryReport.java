package com.marcusprado02.sharedkernel.infrastructure.sms.model;

import java.time.Instant;
import java.util.Map;

public record DeliveryReport(
        String providerId, String messageId, SmsStatus status, String carrierCode,
        String description, Instant timestamp, Map<String, Object> raw
) {}