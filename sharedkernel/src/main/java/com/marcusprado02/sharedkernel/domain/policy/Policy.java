package com.marcusprado02.sharedkernel.domain.policy;

public interface Policy {
  String id();
  String version();             // "v1", "2025-09-01"
  PolicyResult evaluate(EvalContext ctx);
}
