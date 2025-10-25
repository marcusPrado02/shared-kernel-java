package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

public final class ContextScopeBehavior implements AsyncCommandBehavior {
  @Override public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    RequestContextHolder.set(ctx);
    try {
      return next.proceed(cmd, ctx).whenComplete((r,e) -> RequestContextHolder.clear());
    } catch (RuntimeException ex) {
      RequestContextHolder.clear(); throw ex;
    }
  }
}