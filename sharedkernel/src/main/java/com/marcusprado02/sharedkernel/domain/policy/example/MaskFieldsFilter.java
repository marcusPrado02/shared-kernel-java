package com.marcusprado02.sharedkernel.domain.policy.example;

import java.util.Map;

import com.marcusprado02.sharedkernel.domain.policy.PolicyResult;
import com.marcusprado02.sharedkernel.domain.policy.enforcement.ResponseFilter;

public final class MaskFieldsFilter implements ResponseFilter<Map<String,Object>> {
  @Override public Map<String, Object> apply(Map<String, Object> body, PolicyResult r) {
    if (body == null) return null;
    var masked = new java.util.HashMap<>(body);
    r.attributes.getOrDefault("mask", "").lines().forEach(f -> {
      if (masked.containsKey(f)) masked.put(f, "****");
    });
    return java.util.Collections.unmodifiableMap(masked);
  }
}
