package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;


import java.util.concurrent.*;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

import io.github.resilience4j.circuitbreaker.*;

public final class CircuitBreakerBehavior implements AsyncCommandBehavior {
  private final CircuitBreakerRegistry registry;
  private final Function<Class<?>, String> nameFn;

  public CircuitBreakerBehavior(CircuitBreakerRegistry registry, Function<Class<?>,String> nameFn) {
    this.registry = registry; this.nameFn = nameFn;
  }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var cb = registry.circuitBreaker(nameFn.apply(cmd.getClass()));
    try {
      cb.acquirePermission();
      var promise = new CompletableFuture<R>();
      next.proceed(cmd, ctx).whenComplete((res, err) -> {
        if (err == null) { cb.onSuccess(0, java.util.concurrent.TimeUnit.MILLISECONDS); promise.complete(res); }
        else { cb.onError(0, java.util.concurrent.TimeUnit.MILLISECONDS, err); promise.completeExceptionally(err); }
      });
      return promise;
    } catch (CallNotPermittedException e) {
      return CompletableFuture.failedStage(e);
    }
  }
}

