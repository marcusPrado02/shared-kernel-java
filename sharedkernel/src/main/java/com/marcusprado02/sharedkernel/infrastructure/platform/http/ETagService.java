package com.marcusprado02.sharedkernel.infrastructure.platform.http;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ETagService {
    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();

    public String strongETag(Object body) {
        try {
            var json = om.writeValueAsBytes(body);
            var sha = MessageDigest.getInstance("SHA-256").digest(json);
            return "\"" + bytesToHex(sha) + "\"";
        } catch (Exception e) {
            // fallback: timestamp random
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }

    public String strongETagFromVersion(String version) {
        try {
            var sha = MessageDigest.getInstance("SHA-256").digest(version.getBytes(StandardCharsets.UTF_8));
            return "\"" + bytesToHex(sha) + "\"";
        } catch (Exception e) {
            return "\"" + version + "\"";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        var sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
