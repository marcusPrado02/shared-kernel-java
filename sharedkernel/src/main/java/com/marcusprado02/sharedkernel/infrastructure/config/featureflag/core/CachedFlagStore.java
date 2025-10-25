package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.core;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api.model.FlagDefinition;

public final class CachedFlagStore {
  private final ConcurrentHashMap<String, FlagDefinition> cache = new ConcurrentHashMap<>();
  public Optional<FlagDefinition> get(String key) { return Optional.ofNullable(cache.get(key)); }
  public void putAll(Map<String, FlagDefinition> defs) { cache.putAll(defs); }
  public void put(FlagDefinition def) { cache.put(def.key(), def); }
  public void invalidate(String key) { cache.remove(key); }
  public void clear() { cache.clear(); }
}