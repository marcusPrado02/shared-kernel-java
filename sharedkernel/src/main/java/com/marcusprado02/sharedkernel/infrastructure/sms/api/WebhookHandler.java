package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.WebhookResult;

public interface WebhookHandler {
    WebhookResult handleWebhook(String providerId, String body, String signatureHeader);
}
