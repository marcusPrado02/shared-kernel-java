package com.marcusprado02.sharedkernel.infrastructure.resilience.hedging;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.ExecutionContext;

public interface HedgeDecider {
  record Decision(boolean shouldHedge, HedgingPolicy.Mode mode, HedgingPolicy.Quorum quorum, int maxCopies, long staggerNanos) {}
  Decision decide(ExecutionContext ctx);
}
