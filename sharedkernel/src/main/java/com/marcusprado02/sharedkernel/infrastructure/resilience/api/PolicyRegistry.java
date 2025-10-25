package com.marcusprado02.sharedkernel.infrastructure.resilience.api;

import java.util.Optional;
public interface PolicyRegistry {
  <P extends Policy> void register(P policy);
  <P extends Policy> Optional<P> get(PolicyKey key, Class<P> type);
  <P extends Policy> P require(PolicyKey key, Class<P> type);
  boolean contains(PolicyKey key);
}