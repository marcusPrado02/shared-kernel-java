package com.marcusprado02.sharedkernel.crosscutting.hash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class MessageDigestHolder {
    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    });

    private MessageDigestHolder() {}

    public static String sha256Hex(String s) {
        MessageDigest md = SHA256.get();
        md.reset();
        byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes);
    }
}