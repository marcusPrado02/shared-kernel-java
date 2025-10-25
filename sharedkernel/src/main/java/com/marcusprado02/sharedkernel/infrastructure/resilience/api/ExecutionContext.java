package com.marcusprado02.sharedkernel.infrastructure.resilience.api;

import java.time.Duration;
import java.util.Map;
public record ExecutionContext(
  String operation,            // ex: "GET /payments/{id}"
  Map<String, Object> tags,    // tenant, region, replica, method, resource
  Duration budget,             // orçamento de tempo restante (SLO-aware)
  long startNanoTime
) {
  public static ExecutionContext of(String op, Map<String,Object> tags, Duration budget) {
    return new ExecutionContext(op, tags, budget, System.nanoTime());
  }
}
