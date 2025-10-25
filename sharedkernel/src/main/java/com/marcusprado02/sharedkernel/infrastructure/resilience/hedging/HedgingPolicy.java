package com.marcusprado02.sharedkernel.infrastructure.resilience.hedging;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.ExecutionContext;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.Policy;

public interface HedgingPolicy extends Policy {

  enum Mode { PARALLEL, STAGGERED }
  enum Quorum { ANY, FIRST_SUCCESS, MAJORITY, ALL }

  record Candidate(String id, Map<String,Object> hints) {} // ex.: replica="us-east-1a", pool="ro"
  record Plan(Mode mode, Quorum quorum, int maxCopies, long staggerNanos) {}

  interface CandidateProvider {
    List<Candidate> select(ExecutionContext ctx);
  }

  <T> CompletableFuture<T> executeAsync(
    ExecutionContext ctx,
    Supplier<CompletableFuture<T>> primary,
    List<Supplier<CompletableFuture<T>>> hedges,
    Plan plan
  );

  <T> CompletableFuture<T> executeAdaptive(
    ExecutionContext ctx,
    Supplier<CompletableFuture<T>> base,
    CandidateProvider candidates // provider fabrica suppliers a partir de hints/rotas
  );
}