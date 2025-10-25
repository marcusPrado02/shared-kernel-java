package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;


import java.time.Duration;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class RetryBehavior implements AsyncCommandBehavior {
  private final int maxAttempts;
  private final Duration baseDelay;
  private final double multiplier;
  private final boolean jitter;
  private final ScheduledExecutorService scheduler;

  public RetryBehavior(int maxAttempts, Duration baseDelay, double multiplier, boolean jitter, ScheduledExecutorService scheduler) {
    this.maxAttempts = maxAttempts; this.baseDelay = baseDelay; this.multiplier = multiplier; this.jitter = jitter; this.scheduler = scheduler;
  }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    return attempt(cmd, ctx, next, 1);
  }

  private <C extends Command<R>, R> CompletionStage<R> attempt(C cmd, CommandContext ctx, Next<C,R> next, int n) {
    var promise = new CompletableFuture<R>();
    next.proceed(cmd, ctx).whenComplete((res, err) -> {
      if (err == null || n >= maxAttempts) { if (err == null) promise.complete(res); else promise.completeExceptionally(err); return; }
      long delayMs = (long)(baseDelay.toMillis() * Math.pow(multiplier, n-1));
      if (jitter) delayMs = (long)(delayMs * (0.5 + Math.random()));
      scheduler.schedule(() -> attempt(cmd, ctx, next, n+1).whenComplete((r2,e2) -> { if (e2==null) promise.complete(r2); else promise.completeExceptionally(e2); }),
          delayMs, TimeUnit.MILLISECONDS);
    });
    return promise;
    }
}
