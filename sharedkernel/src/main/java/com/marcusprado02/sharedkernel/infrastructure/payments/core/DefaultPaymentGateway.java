package com.marcusprado02.sharedkernel.infrastructure.payments.core;


import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.api.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.*;

import com.marcusprado02.sharedkernel.infrastructure.payments.core.SignatureRegistry;

public class DefaultPaymentGateway implements PaymentGateway {

    private final Map<String, ProviderAdapter> providers = new HashMap<>();
    private final PaymentRouter router;
    private final IdempotencyStore idempotency;
    private final OutboxPublisher outbox;
    private final SignatureRegistry signatures;
    private final Map<String, Capabilities> caps = new HashMap<>();

    public DefaultPaymentGateway(Collection<ProviderFactory> factories,
                                 ProviderConfigResolver configResolver,
                                 PaymentRouter router,
                                 IdempotencyStore idempotency,
                                 OutboxPublisher outbox,
                                 SignatureRegistry signatures) {
        this.router = router; this.idempotency = idempotency; this.outbox = outbox; this.signatures = signatures;

        for (ProviderFactory f : factories) {
            ProviderConfig cfg = configResolver.resolve(f.providerId());
            ProviderAdapter adapter = f.create(cfg);
            providers.put(f.providerId(), adapter);
            caps.put(f.providerId(), adapter.metadata().capabilities());
        }
    }

    private ProviderAdapter pick(PaymentContext ctx) {
        String providerId = Optional.ofNullable(ctx.preferredProvider()).orElseGet(() -> router.route(ctx));
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) throw new IllegalArgumentException("Provider not found: " + providerId);
        return adapter;
    }

    // --- PaymentOperations
    @Override public PaymentResponse authorize(PaymentRequest req, Policy policy) {
        return idempotency.findPayment(req.idempotencyKey()).orElseGet(() -> {
            PaymentResponse res = pick(req.context()).authorize(req, policy);
            idempotency.savePayment(req.idempotencyKey(), res);
            if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.AUTHORIZED", res);
            return res;
        });
    }

    @Override public PaymentResponse capture(CaptureRequest req, Policy policy) {
        PaymentResponse res = pick(req.context()).capture(req, policy);
        if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.CAPTURED", res);
        return res;
    }

    @Override public PaymentResponse sale(PaymentRequest req, Policy policy) {
        return idempotency.findPayment(req.idempotencyKey()).orElseGet(() -> {
            PaymentResponse res = pick(req.context()).sale(req, policy);
            idempotency.savePayment(req.idempotencyKey(), res);
            if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.SALE", res);
            return res;
        });
    }

    @Override public PaymentResponse voidAuth(VoidRequest req, Policy policy) {
        PaymentResponse res = pick(req.context()).voidAuth(req, policy);
        if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.VOID", res);
        return res;
    }

    @Override public CompletableFuture<PaymentResponse> authorizeAsync(PaymentRequest req, Policy p) { return pick(req.context()).authorizeAsync(req, p); }
    @Override public CompletableFuture<PaymentResponse> captureAsync(CaptureRequest req, Policy p) { return CompletableFuture.supplyAsync(() -> capture(req, p)); }
    @Override public CompletableFuture<PaymentResponse> saleAsync(PaymentRequest req, Policy p) { return CompletableFuture.supplyAsync(() -> sale(req, p)); }
    @Override public CompletableFuture<PaymentResponse> voidAuthAsync(VoidRequest req, Policy p) { return CompletableFuture.supplyAsync(() -> voidAuth(req, p)); }

    @Override public PaymentDetails getDetails(String paymentId, PaymentContext ctx) {
        return pick(ctx).getDetails(paymentId, ctx);
    }

    // --- Refunds
    @Override public RefundResponse refund(RefundRequest req, Policy policy) {
        return idempotency.findRefund(req.idempotencyKey()).orElseGet(() -> {
            RefundResponse res = pick(req.context()).refund(req, policy);
            idempotency.saveRefund(req.idempotencyKey(), res);
            if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.REFUNDED", res);
            return res;
        });
    }
    @Override public CompletableFuture<RefundResponse> refundAsync(RefundRequest req, Policy p) { return pick(req.context()).refundAsync(req, p); }

    // --- Payouts
    @Override public PayoutResponse payout(PayoutRequest req, Policy policy) {
        PayoutResponse res = pick(req.context()).payout(req, policy);
        if (policy.outboxEnabled()) outbox.publishPaymentEvent("PAYMENT.PAYOUT", res);
        return res;
    }
    @Override public CompletableFuture<PayoutResponse> payoutAsync(PayoutRequest req, Policy p) { return CompletableFuture.supplyAsync(() -> payout(req, p)); }

    // --- Webhook
    @Override public WebhookResult handleWebhook(String providerId, String body, String signatureHeader) {
        ProviderAdapter adapter = providers.get(providerId);
        if (adapter == null) return new WebhookResult(false, "unknown provider");
        return adapter.handleWebhook(body, signatureHeader, signatures::resolveSecret);
    }

    @Override public java.util.Optional<String> resolveProvider(PaymentContext ctx) { return java.util.Optional.ofNullable(router.route(ctx)); }
    @Override public Capabilities capabilities(String providerId) { return caps.get(providerId); }
}

