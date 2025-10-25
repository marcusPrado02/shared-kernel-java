package com.marcusprado02.sharedkernel.domain.policy;

import java.util.*;
import java.util.function.Supplier;

public interface PolicyCache {
  PolicyResult getOrCompute(String key, Supplier<PolicyResult> sup);
  static PolicyCache lru(int max) {
    return new PolicyCache() {
      private final Map<String,PolicyResult> map = new java.util.LinkedHashMap<>(16,0.75f,true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String,PolicyResult> e){ return size()>max; }
      };
      public synchronized PolicyResult getOrCompute(String k, Supplier<PolicyResult> s){
        return map.computeIfAbsent(k, kk -> s.get());
      }
    };
  }
}
