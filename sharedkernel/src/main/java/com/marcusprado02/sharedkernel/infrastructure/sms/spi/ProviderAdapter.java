package com.marcusprado02.sharedkernel.infrastructure.sms.spi;


import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.sms.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;

public interface ProviderAdapter {
    ProviderMetadata metadata();

    SmsResponse send(SmsRequest req, Policy policy);
    List<SmsResponse> sendBulk(BulkRequest req, Policy policy);
    CompletableFuture<SmsResponse> sendAsync(SmsRequest req, Policy policy);
    CompletableFuture<List<SmsResponse>> sendBulkAsync(BulkRequest req, Policy policy);

    DeliveryReport getDeliveryReport(String messageId, SmsContext ctx);

    WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver);
}
