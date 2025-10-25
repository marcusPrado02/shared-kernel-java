package com.marcusprado02.sharedkernel.infrastructure.email.core;


/** Resolve o segredo/assinatura de um provider (para validação de webhooks). */
@FunctionalInterface
public interface SignatureRegistry {
    String resolveSecret(String providerId);
}