package com.marcusprado02.sharedkernel.infrastructure.resilience.hedging;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.ExecutionContext;

public interface HedgeCostController {
  int acquireCopies(ExecutionContext ctx, int requested);
  void releaseCopies(ExecutionContext ctx);
}

