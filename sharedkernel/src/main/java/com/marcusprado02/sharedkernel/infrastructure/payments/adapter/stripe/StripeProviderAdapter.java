package com.marcusprado02.sharedkernel.infrastructure.payments.adapter.stripe;


import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.spi.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.payments.core.BaseProviderAdapter;
import com.marcusprado02.sharedkernel.infrastructure.payments.model.*;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.Capabilities;

public class StripeProviderAdapter extends BaseProviderAdapter {

    private final ProviderConfig cfg;
    private final String providerId = "stripe";

    public StripeProviderAdapter(ProviderConfig cfg) { this.cfg = cfg; }

    @Override public ProviderMetadata metadata() {
        return new ProviderMetadata(providerId, "Stripe", "1.0",
                java.util.Set.of("US","BR","EU"),
                new Capabilities(true, true, true, true,
                        java.util.Set.of("CARD_TOKEN","WALLET","PIX")));
    }

    @Override public PaymentResponse authorize(PaymentRequest req, Policy policy) {
        return withPolicy(policy, () -> {
            // 1) mapear request -> API provider
            // 2) chamar /payment_intents
            // 3) tratar 3DS (status PENDING + threeDSStatus = "REQUIRED")
            // 4) mapear resposta -> PaymentResponse
            return buildResponseOk(providerId, "ch_123", Map.of("raw", "ok"));
        });
    }

    @Override public PaymentResponse capture(CaptureRequest req, Policy policy) {
        return withPolicy(policy, () -> buildResponseOk(providerId, req.paymentId(), Map.of("captured", true)));
    }

    @Override public PaymentResponse sale(PaymentRequest req, Policy policy) {
        // auth + capture numa tacada, conforme flag req.capture()
        return withPolicy(policy, () -> buildResponseOk(providerId, "ch_sale_123", Map.of()));
    }

    @Override public PaymentResponse voidAuth(VoidRequest req, Policy policy) {
        return withPolicy(policy, () -> buildDeclined(providerId, PaymentErrorCode.CANCELED, Map.of("void", true)));
    }

    @Override public PaymentDetails getDetails(String paymentId, PaymentContext ctx) {
        return new PaymentDetails(paymentId, providerId, Map.of("status","CAPTURED"));
    }

    @Override public RefundResponse refund(RefundRequest req, Policy policy) {
        return withPolicyRefund(policy, () -> new RefundResponse("rf_123", PaymentStatus.REFUNDED,
                PaymentErrorCode.NONE, providerId, "rfp_123", Map.of("amount", req.amount().amount())));
    }

    @Override public PayoutResponse payout(PayoutRequest req, Policy policy) {
        return new PayoutResponse("po_123", PaymentStatus.PENDING, PaymentErrorCode.NONE, providerId, "pp_123", Map.of());
    }

    @Override public CompletableFuture<PaymentResponse> authorizeAsync(PaymentRequest req, Policy policy) {
        return super.authorizeAsync(req, policy);
    }

    @Override public WebhookResult handleWebhook(String body, String signatureHeader, SignatureResolver resolver) {
        String secret = resolver.resolveSecret(providerId);
        // Verificar assinatura (ex.: t=..., v1=...)
        boolean ok = true; // chame SignatureVerifier adequado
        return new WebhookResult(ok, ok ? "accepted" : "invalid-signature");
    }
}


