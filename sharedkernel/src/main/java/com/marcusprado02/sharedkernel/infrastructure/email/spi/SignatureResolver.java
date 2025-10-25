package com.marcusprado02.sharedkernel.infrastructure.email.spi;


@FunctionalInterface
public interface SignatureResolver {
    /** Retorna o segredo/chave para validação de webhook do provider. */
    String resolveSecret(String providerId);
}