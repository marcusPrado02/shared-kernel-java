package com.marcusprado02.sharedkernel.infrastructure.payments.api;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.payments.model.PaymentContext;
import com.marcusprado02.sharedkernel.infrastructure.payments.spi.Capabilities;

public interface PaymentGateway extends
        PaymentOperations, RefundOperations, PayoutOperations, WebhookHandler {

    /** Resolve o provider em uso p/ uma chave (tenant, loja, preferência do meio). */
    Optional<String> resolveProvider(PaymentContext ctx);

    /** Exposição para introspecção/feature flags por provider. */
    Capabilities capabilities(String providerId);
}
