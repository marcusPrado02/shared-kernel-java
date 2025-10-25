package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;
import com.marcusprado02.sharedkernel.infrastructure.sms.spi.Capabilities;

public interface SmsGateway extends SendOperations, WebhookHandler {
    Optional<String> resolveProvider(SmsContext ctx);
    Capabilities capabilities(String providerId);

    // Utilidades
    DeliveryReport getDeliveryReport(String providerId, String messageId, SmsContext ctx);
    List<DeliveryReport> getDeliveryReportsBulk(String providerId, List<String> messageIds, SmsContext ctx);
}
