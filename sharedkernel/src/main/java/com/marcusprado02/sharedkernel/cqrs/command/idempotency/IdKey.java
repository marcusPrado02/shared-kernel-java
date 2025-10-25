package com.marcusprado02.sharedkernel.cqrs.command.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public record IdKey(String tenantId, String scope, String naturalKey) {

  public String hash() {
    try {
      var data = scope + ":" + tenantId + ":" + naturalKey;
      var md = MessageDigest.getInstance("SHA-256");
      var bytes = md.digest(data.getBytes(StandardCharsets.UTF_8));
      var sb = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to compute idempotency hash", e);
    }
  }
}
