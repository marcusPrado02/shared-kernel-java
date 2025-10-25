package com.marcusprado02.sharedkernel.infrastructure.resilience.core;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.CheckedSupplier;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.ExecutionContext;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.PolicyKey;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.PolicyRegistry;
import com.marcusprado02.sharedkernel.infrastructure.resilience.hedging.HedgingPolicy;
import com.marcusprado02.sharedkernel.infrastructure.resilience.policies.*;

public final class PolicyComposer {

  public static <T> T sync(ExecutionContext ctx, PolicyRegistry reg, PolicyKey baseKey,
                           CheckedSupplier<T> supplier) throws Exception {
    // Ordem recomendada: Bulkhead -> RateLimiter -> Timeout -> Circuit -> Retry -> Fallback
    var bulkhead = reg.get(baseKey, BulkheadPolicy.class).orElse(null);
    var limiter  = reg.get(baseKey, RateLimiterPolicy.class).orElse(null);
    var timeout  = reg.get(baseKey, TimeoutPolicy.class).orElse(null);
    var circuit  = reg.get(baseKey, CircuitBreakerPolicy.class).orElse(null);
    var retry    = reg.get(baseKey, RetryPolicy.class).orElse(null);
    var fallback = reg.get(baseKey, FallbackPolicy.class).orElse(null);

    CheckedSupplier<T> chain = supplier;

    if (fallback != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> fallback.execute(ctx, prev);
    }
    if (retry != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> retry.execute(ctx, prev);
    }
    if (circuit != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> circuit.execute(ctx, prev);
    }
    if (timeout != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> timeout.execute(ctx, prev);
    }
    if (limiter != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> limiter.execute(ctx, prev);
    }
    if (bulkhead != null) {
      final CheckedSupplier<T> prev = chain;
      chain = () -> bulkhead.execute(ctx, prev);
    }

    return chain.get();
  }

  public static <T> CompletableFuture<T> async(ExecutionContext ctx, PolicyRegistry reg, PolicyKey baseKey,
                                               Supplier<CompletableFuture<T>> supplier) {
    var hedging = reg.get(baseKey, HedgingPolicy.class).orElse(null);
    Supplier<CompletableFuture<T>> chain = supplier;

    if (hedging != null) {
      return hedging.executeAdaptive(ctx, chain, adaptiveCandidates(reg, baseKey));
    }
    return chain.get();
  }

  private static HedgingPolicy.CandidateProvider adaptiveCandidates(PolicyRegistry reg, PolicyKey key) {
    return ctx -> java.util.List.of(
      new HedgingPolicy.Candidate("replica-a", java.util.Map.of("region", "us-east-1a")),
      new HedgingPolicy.Candidate("replica-b", java.util.Map.of("region", "us-east-1b"))
    );
  }
}
