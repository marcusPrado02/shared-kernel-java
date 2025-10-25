package com.marcusprado02.sharedkernel.infrastructure.sms.model;

import java.util.Map;

public record SmsContext(
        String tenantId, String senderId, String country, String campaignId,
        String preferredProvider, Map<String, Object> tags
) {}

