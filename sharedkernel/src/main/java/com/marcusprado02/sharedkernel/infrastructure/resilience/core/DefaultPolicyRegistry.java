package com.marcusprado02.sharedkernel.infrastructure.resilience.core;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.PolicyKey;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.PolicyRegistry;

public final class DefaultPolicyRegistry implements PolicyRegistry {
  private final Map<String, Policy> byKey = new ConcurrentHashMap<>();
  private final Map<Class<?>, Map<String, Policy>> byType = new ConcurrentHashMap<>();

  @Override public <P extends Policy> void register(P policy) {
    var k = policy.key().fq();
    byKey.put(k, policy);
    byType.computeIfAbsent(policy.getClass().getInterfaces()[0], t -> new ConcurrentHashMap<>()).put(k, policy);
  }
  @SuppressWarnings("unchecked")
  @Override public <P extends Policy> Optional<P> get(PolicyKey key, Class<P> type) {
    var map = byType.getOrDefault(type, Map.of());
    var p = map.get(key.fq());
    return Optional.ofNullable((P) p);
  }
  @Override public <P extends Policy> P require(PolicyKey key, Class<P> type) {
    return get(key, type).orElseThrow(() -> new NoSuchElementException("Policy not found: " + key.fq() + " type=" + type.getSimpleName()));
  }
  @Override public boolean contains(PolicyKey key) { return byKey.containsKey(key.fq()); }
}