package com.marcusprado02.sharedkernel.infrastructure.email.spi;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.email.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.email.model.*;

public interface ProviderAdapter {
    ProviderMetadata metadata();

    EmailResponse send(EmailRequest req, Policy policy);
    List<EmailResponse> sendBulk(BulkRequest req, Policy policy);

    CompletableFuture<EmailResponse> sendAsync(EmailRequest req, Policy policy);
    CompletableFuture<List<EmailResponse>> sendBulkAsync(BulkRequest req, Policy policy);

    EmailEvent getStatus(String messageId, EmailContext ctx);

    WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver);

    InboundResult handleInbound(byte[] rawMime);
}
