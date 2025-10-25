package com.marcusprado02.sharedkernel.infrastructure.email.api;

import com.marcusprado02.sharedkernel.infrastructure.email.model.WebhookResult;

public interface WebhookHandler {
    WebhookResult handleWebhook(String providerId, String body, String signatureHeader);
}
