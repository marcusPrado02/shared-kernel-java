package com.marcusprado02.sharedkernel.infrastructure.sms.spi;

/** Resolve o segredo/chave pública usada para verificar a assinatura do webhook do provider. */
@FunctionalInterface
public interface SignatureResolver {
    /**
     * Retorna o material de chave para o provider (ex.: secret HMAC, chave pública JWS/JWKS).
     * @param providerId identificador do provider ("twilio", "sns", etc.)
     * @return segredo/chave em formato adequado ao verificador que você usa.
     */
    String resolveSecret(String providerId);
}