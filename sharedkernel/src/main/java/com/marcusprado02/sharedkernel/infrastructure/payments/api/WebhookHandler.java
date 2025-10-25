package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.WebhookResult;

public interface WebhookHandler {
    /** Verifica, decodifica e roteia eventos de webhook vindos do provider. */
    WebhookResult handleWebhook(String providerId, String body, String signatureHeader);
}
