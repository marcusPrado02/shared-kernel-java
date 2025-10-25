package com.marcusprado02.sharedkernel.infrastructure.blob;

public record Encryption(
        Type type,
        String kmsKeyId,      // KMS key (KMS/CMK) quando aplicável
        byte[] customerKey    // CSE: chave do cliente (em memória com extremo cuidado)
) {
    public enum Type { NONE, SSE_S3, SSE_KMS, CSE }
    public static Encryption none(){ return new Encryption(Type.NONE, null, null); }
}
