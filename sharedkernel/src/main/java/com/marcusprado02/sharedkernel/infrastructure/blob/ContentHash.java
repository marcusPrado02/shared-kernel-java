package com.marcusprado02.sharedkernel.infrastructure.blob;

public record ContentHash(String algorithm, String valueBase64) {
    public static ContentHash md5(String base64){ return new ContentHash("MD5", base64); }
    public static ContentHash sha256(String base64){ return new ContentHash("SHA-256", base64); }
}