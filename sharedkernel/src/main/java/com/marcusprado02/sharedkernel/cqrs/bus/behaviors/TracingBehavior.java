package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

import io.opentelemetry.api.trace.Tracer;

public final class TracingBehavior implements AsyncCommandBehavior {
  private final Tracer tracer;
  public TracingBehavior(Tracer t){ this.tracer=t; }

  @Override public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var span = tracer.spanBuilder("CMD " + cmd.getClass().getSimpleName())
        .setAttribute("corrId", ctx.correlationId().orElse("new"))
        .setAttribute("tenant", ctx.tenantId().orElse("unknown")).startSpan();

    try (var scope = span.makeCurrent()) {
      return next.proceed(cmd, ctx).whenComplete((r,e) -> {
        if (e != null) { span.recordException(e); span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR); }
        span.end();
      });
    }
  }
}
