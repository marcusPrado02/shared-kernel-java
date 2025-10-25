package com.marcusprado02.sharedkernel.infrastructure.resilience.hedging;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import com.marcusprado02.sharedkernel.infrastructure.resilience.api.CheckedSupplier;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.ExecutionContext;
import com.marcusprado02.sharedkernel.infrastructure.resilience.api.PolicyKey;

public final class DefaultHedgingPolicy implements HedgingPolicy {
  private final PolicyKey key;
  private final MeterRegistry meters;
  private final Tracer tracer;
  private final ScheduledExecutorService scheduler; // para STAGGERED
  private final HedgeDecider decider;               // quando e quanto hedgear
  private final HedgeCostController cost;           // rate/copies/budget

  public DefaultHedgingPolicy(PolicyKey key, MeterRegistry m, Tracer t,
                              ScheduledExecutorService scheduler,
                              HedgeDecider decider, HedgeCostController cost) {
    this.key = key; this.meters = m; this.tracer = t; this.scheduler = scheduler; this.decider = decider; this.cost = cost;
  }

  @Override public PolicyKey key() { return key; }

  @Override
  public <T> CompletableFuture<T> executeAsync(ExecutionContext ctx,
                                               Supplier<CompletableFuture<T>> primary,
                                               List<Supplier<CompletableFuture<T>>> hedges,
                                               Plan plan) {
    var span = tracer.spanBuilder("hedge.execute").setAttribute("policy.key", key.fq())
      .setAttribute("op", ctx.operation()).startSpan();
    try (var scope = span.makeCurrent()) {
      meters.counter("hedge.invocations", "policy", key.fq()).increment();
      // aplica quotas/custos
      var allowedCopies = cost.acquireCopies(ctx, plan.maxCopies());
      var futures = new ArrayList<CompletableFuture<T>>();
      var result = new CompletableFuture<T>();

      // primary dispara já
      futures.add(wrap(primary.get(), "primary"));

      // dispara hedges conforme Mode
      if (plan.mode() == Mode.PARALLEL) {
        for (int i = 0; i < Math.min(hedges.size(), (int) allowedCopies); i++) {
          futures.add(wrap(hedges.get(i).get(), "hedge-"+i));
        }
      } else { // STAGGERED
        for (int i = 0; i < Math.min(hedges.size(), (int) allowedCopies); i++) {
          int idx = i;
          scheduler.schedule(() -> futures.add(wrap(hedges.get(idx).get(), "hedge-"+idx)),
                             plan.staggerNanos(), TimeUnit.NANOSECONDS);
        }
      }

      // composição por quorum
      switch (plan.quorum()) {
        case ANY, FIRST_SUCCESS -> raceFirstSuccess(futures, result);
        case MAJORITY -> raceMajority(futures, result);
        case ALL -> awaitAll(futures, result);
      }

      // cancel losers/cleanup
      result.whenComplete((v, ex) -> futures.forEach(f -> { if (!f.isDone()) f.cancel(true); }));
      return result.whenComplete((v, ex) -> {
        if (ex == null) {
          meters.counter("hedge.success", "policy", key.fq()).increment();
          span.setAttribute("result", "success");
        } else {
          meters.counter("hedge.failure", "policy", key.fq(), "cause", ex.getClass().getSimpleName()).increment();
          span.setAttribute("result", "failure");
          span.recordException(ex);
        }
        span.end();
        cost.releaseCopies(ctx);
      });

    } finally {
      // scope closed
    }
  }

  @Override
  public <T> CompletableFuture<T> executeAdaptive(ExecutionContext ctx,
                                                  Supplier<CompletableFuture<T>> base,
                                                  CandidateProvider providers) {
    var decision = decider.decide(ctx);
    if (!decision.shouldHedge()) {
      return executeAsync(ctx, base, List.of(), new Plan(Mode.PARALLEL, Quorum.FIRST_SUCCESS, 0, 0));
    }
    // fabrica suppliers a partir de candidates
    List<Supplier<CompletableFuture<T>>> hedges = new ArrayList<>();
    for (var c : providers.select(ctx)) {
      hedges.add(() -> base.get()); // ou construir supplier diferenciado com rota/replica a partir de hints
      if (hedges.size() >= decision.maxCopies()) break;
    }
    return executeAsync(ctx, base, hedges, new Plan(decision.mode(), decision.quorum(), decision.maxCopies(), decision.staggerNanos()));
  }

  private <T> CompletableFuture<T> wrap(CompletableFuture<T> f, String label) {
    return f.whenComplete((v, ex) -> meters.counter("hedge.attempt", "label", label, "policy", key.fq(), "status", ex==null?"ok":"err").increment());
  }

  // Helpers quorum
  private <T> void raceFirstSuccess(List<CompletableFuture<T>> futures, CompletableFuture<T> result) {
    futures.forEach(f -> f.thenAccept(v -> result.complete(v))
                          .exceptionally(ex -> { if (allDoneWithException(futures)) result.completeExceptionally(ex); return null; }));
  }
  private <T> void raceMajority(List<CompletableFuture<T>> futures, CompletableFuture<T> result) {
    int need = futures.size()/2 + 1;
    final int[] ok = {0}; final int[] fail = {0};
    futures.forEach(f -> f.whenComplete((v, ex) -> {
      if (ex == null && ++ok[0] >= need) result.complete(v);
      if (ex != null && ++fail[0] > futures.size() - need) result.completeExceptionally(ex);
    }));
  }
  private <T> void awaitAll(List<CompletableFuture<T>> futures, CompletableFuture<T> result) {
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
      .whenComplete((v, ex) -> {
        if (ex != null) result.completeExceptionally(ex);
        else result.complete(futures.get(0).join()); // qualquer (ou combine)
      });
  }
  
  private boolean allDoneWithException(List<?> futures) { return futures.stream().allMatch(f -> ((CompletableFuture<?>)f).isCompletedExceptionally()); }


  @Override
  public <T> T execute(ExecutionContext ctx, CheckedSupplier<T> supplier) throws Exception {
    // Hedging é essencialmente assíncrono; no fluxo síncrono fazemos pass-through.
    // Se quiser, dá para ligar vthreads aqui futuramente.
    return supplier.get();
  }

  @Override
  public <T> CompletableFuture<T> executeAsync(ExecutionContext ctx,
                                              Supplier<CompletableFuture<T>> supplier) {
    // Implementação básica: consulta o decider e cria N cópias do mesmo supplier,
    // caso você não tenha um CandidateProvider externo.
    var d = decider.decide(ctx);
    var hedges = new ArrayList<Supplier<CompletableFuture<T>>>();
    for (int i = 0; i < Math.max(0, d.maxCopies()); i++) {
      hedges.add(() -> supplier.get());
    }
    return executeAsync(ctx, supplier, hedges,
        new Plan(d.mode(), d.quorum(), d.maxCopies(), d.staggerNanos()));
  }
}

