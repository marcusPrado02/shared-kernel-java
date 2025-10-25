package com.marcusprado02.sharedkernel.infrastructure.payments.security;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.marcusprado02.sharedkernel.infrastructure.payments.api.SignatureVerifier;

import java.util.Base64;

public class HmacSignatureVerifier implements SignatureVerifier {
    private final String algorithm; // "HmacSHA256"
    public HmacSignatureVerifier(String algorithm) { this.algorithm = algorithm; }

    @Override
    public boolean verify(String payload, String signatureHeader, String secret) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(), algorithm));
            byte[] raw = mac.doFinal(payload.getBytes());
            String expected = Base64.getEncoder().encodeToString(raw);
            return constantTimeEquals(expected, signatureHeader);
        } catch (Exception e) { return false; }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        if (a.length() != b.length()) return false;
        int r = 0; for (int i=0;i<a.length();i++) r |= a.charAt(i) ^ b.charAt(i);
        return r == 0;
    }
}