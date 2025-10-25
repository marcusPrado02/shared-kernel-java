package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.Map;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class AuditBehavior implements AsyncCommandBehavior {
  private final AuditSink sink;

  public AuditBehavior(AuditSink sink) { this.sink = sink; }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    long t0 = System.nanoTime();
    var action = cmd.getClass().getSimpleName();
    var tenant = ctx.tenantId().orElse("unknown");
    var user   = ctx.userId().orElse("anonymous");

    return next.proceed(cmd, ctx).whenComplete((res, err) -> {
      long ms = (System.nanoTime() - t0)/1_000_000L;
      sink.record(action, tenant, user, err==null, ms, ctx.headers(), err);
    });
  }
}
