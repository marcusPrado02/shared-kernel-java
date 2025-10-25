package com.marcusprado02.sharedkernel.infrastructure.sms.core;

/** Resolve o segredo/chave para verificação de webhooks por provider. */
@FunctionalInterface
public interface SignatureRegistry {
    String resolveSecret(String providerId);
}
