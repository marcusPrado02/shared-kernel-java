package com.marcusprado02.sharedkernel.infrastructure.email.security;

/** Contrato para validar assinaturas de webhook (HMAC etc.). */
@FunctionalInterface
public interface SignatureVerifier {
    /**
     * @param payload corpo bruto recebido
     * @param signatureHeader cabeçalho enviado pelo provedor (ex.: X-Signature)
     * @param secret segredo compartilhado para HMAC/validação
     * @return true se assinatura válida
     */
    boolean verify(String payload, String signatureHeader, String secret);
}
