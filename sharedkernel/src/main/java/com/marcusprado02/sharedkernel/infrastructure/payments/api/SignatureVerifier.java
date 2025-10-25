package com.marcusprado02.sharedkernel.infrastructure.payments.api;

public interface SignatureVerifier {
    boolean verify(String payload, String signatureHeader, String secretOrKeyMaterial);
}
