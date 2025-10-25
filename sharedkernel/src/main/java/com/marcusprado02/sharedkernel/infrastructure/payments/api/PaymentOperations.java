package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;

public interface PaymentOperations {
    PaymentResponse authorize(PaymentRequest req, Policy policy);
    PaymentResponse capture(CaptureRequest req, Policy policy);
    PaymentResponse sale(PaymentRequest req, Policy policy); // auth + capture
    PaymentResponse voidAuth(VoidRequest req, Policy policy);

    CompletableFuture<PaymentResponse> authorizeAsync(PaymentRequest req, Policy policy);
    CompletableFuture<PaymentResponse> captureAsync(CaptureRequest req, Policy policy);
    CompletableFuture<PaymentResponse> saleAsync(PaymentRequest req, Policy policy);
    CompletableFuture<PaymentResponse> voidAuthAsync(VoidRequest req, Policy policy);

    PaymentDetails getDetails(String paymentId, PaymentContext ctx);
}
