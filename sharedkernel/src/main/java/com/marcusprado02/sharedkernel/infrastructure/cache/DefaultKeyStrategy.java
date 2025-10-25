package com.marcusprado02.sharedkernel.infrastructure.cache;

import java.util.Optional;

public class DefaultKeyStrategy implements CacheKeyStrategy {
  @Override public String build(String ns, String tenant, String rawKey, Optional<String> version) {
    String v = version.map(s -> ":" + s).orElse("");
    // Ex.: "ns:tenant:v1:sha1(rawKey)"
    String h = sha1(rawKey);
    return "%s:%s%s:%s".formatted(ns, tenant, v, h);
  }
  private String sha1(String s){
    try {
      var md = java.security.MessageDigest.getInstance("SHA-1");
      return java.util.HexFormat.of().formatHex(md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    } catch (Exception e) { throw new RuntimeException(e); }
  }
}