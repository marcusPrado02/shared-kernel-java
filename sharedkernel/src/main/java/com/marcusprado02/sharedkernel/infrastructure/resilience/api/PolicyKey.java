package com.marcusprado02.sharedkernel.infrastructure.resilience.api;

public sealed interface PolicyKey permits NamedPolicyKey {
  String name();
  String namespace(); // "http.client", "db.read", "kafka.producer", etc.
  default String fq() { return namespace() + ":" + name(); }
}
