package com.marcusprado02.sharedkernel.infrastructure.payments.spi;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.api.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;

public interface ProviderAdapter {
    ProviderMetadata metadata();

    PaymentResponse authorize(PaymentRequest req, Policy policy);
    PaymentResponse capture(CaptureRequest req, Policy policy);
    PaymentResponse sale(PaymentRequest req, Policy policy);
    PaymentResponse voidAuth(VoidRequest req, Policy policy);
    PaymentDetails getDetails(String paymentId, PaymentContext ctx);

    RefundResponse refund(RefundRequest req, Policy policy);
    PayoutResponse payout(PayoutRequest req, Policy policy);

    CompletableFuture<PaymentResponse> authorizeAsync(PaymentRequest req, Policy policy);
    CompletableFuture<RefundResponse> refundAsync(RefundRequest req, Policy policy);

    WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver);
}