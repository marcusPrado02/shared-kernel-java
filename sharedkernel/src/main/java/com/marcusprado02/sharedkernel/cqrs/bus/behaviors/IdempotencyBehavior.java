package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;
import com.marcusprado02.sharedkernel.cqrs.command.idempotency.DuplicateIntentException;
import com.marcusprado02.sharedkernel.cqrs.command.idempotency.IdKey;
import com.marcusprado02.sharedkernel.cqrs.command.idempotency.IdempotencyStore;
import java.time.Duration;

public final class IdempotencyBehavior implements AsyncCommandBehavior {
  private final IdempotencyStore store;
  public IdempotencyBehavior(IdempotencyStore s){ this.store=s; }

  @Override public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var tenant = ctx.tenantId().orElse("global");
    var key = "cmd:"+cmd.getClass().getSimpleName()+":"+ctx.correlationId().orElse(UUID.randomUUID().toString());
    var claimed = store.tryClaim(new IdKey(tenant, "cmd", key), Duration.ofMinutes(15), "bus");
    if (!claimed) return CompletableFuture.failedStage(new DuplicateIntentException());

    return next.proceed(cmd, ctx).whenComplete((r,e) -> store.confirm(new IdKey(tenant, "cmd", key)));
  }
}
