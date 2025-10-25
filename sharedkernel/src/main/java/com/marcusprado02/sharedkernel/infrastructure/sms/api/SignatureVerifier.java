package com.marcusprado02.sharedkernel.infrastructure.sms.api;

public interface SignatureVerifier {
    boolean verify(String payload, String signatureHeader, String secretOrKeyMaterial);
}
