package com.marcusprado02.sharedkernel.crypto;

public interface CryptoService {
    /** Descriptografa um valor opaco para objeto de domínio. */
    Object decrypt(CipherText cipher, String profile);
    /** Criptografa um objeto de domínio para valor opaco (ex.: antes de persistir ou retornar). */
    Object encryptObject(Object value, String profile);
}