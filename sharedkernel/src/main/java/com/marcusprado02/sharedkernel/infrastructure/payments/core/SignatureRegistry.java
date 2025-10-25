package com.marcusprado02.sharedkernel.infrastructure.payments.core;

/** Resolve o segredo/chave para verificação de webhooks por provider de pagamentos. */
@FunctionalInterface
public interface SignatureRegistry {
    String resolveSecret(String providerId);
}