package com.marcusprado02.sharedkernel.crosscutting.decorators.core;

import java.util.Map;

/** Abstração de assinaturas (HMAC, RSA etc). Implementações sabem como assinar inputs/headers. */
public interface Signer {
    /** Assina um payload arbitrário e devolve a representação (ex.: Base64 HMAC). */
    String signBytes(byte[] payload);

    /** Produz a assinatura canônica de um conjunto de headers/campos. */
    String signHeaders(Map<String,String> canonical);

    /** Nome/algoritmo da assinatura (ex.: "HMAC-SHA256"). */
    String algorithm();
}
