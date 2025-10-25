package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.util.Map;

public interface CryptoCapabilities {
    byte[] encrypt(String keyAlias, byte[] plaintext, Map<String,String> aad);
    byte[] decrypt(String keyAlias, byte[] ciphertext, Map<String,String> aad);
    byte[] sign(String keyAlias, byte[] message);
    boolean verify(String keyAlias, byte[] message, byte[] signature);
    default boolean isSupported(){ return true; }
}
