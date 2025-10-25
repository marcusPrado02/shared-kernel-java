package com.marcusprado02.sharedkernel.infrastructure.payments.spi;

/** Resolve o segredo/chave pública usada para verificar a assinatura de webhooks do provider. */
@FunctionalInterface
public interface SignatureResolver {
    String resolveSecret(String providerId);
}