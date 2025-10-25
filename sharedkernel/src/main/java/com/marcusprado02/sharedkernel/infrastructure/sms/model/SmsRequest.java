package com.marcusprado02.sharedkernel.infrastructure.sms.model;

import java.util.Locale;
import java.util.Map;

public record SmsRequest(
        String idempotencyKey, PhoneNumber to, String body, // body OU templateId
        String templateId, Map<String, Object> params, Locale locale,
        SmsContext context, Priority priority, Integer maxLength, Encoding forcedEncoding,
        Map<String, Object> metadata, boolean requireDeliveryReport
) {
    public SmsRequest {
        if ((body == null || body.isBlank()) && (templateId == null || templateId.isBlank()))
            throw new IllegalArgumentException("body ou templateId obrigatório");
    }
}
