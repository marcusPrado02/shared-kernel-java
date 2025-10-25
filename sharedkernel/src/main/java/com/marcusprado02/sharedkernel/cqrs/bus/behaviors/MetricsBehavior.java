package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public final class MetricsBehavior implements AsyncCommandBehavior {
  private final MeterRegistry reg;
  public MetricsBehavior(MeterRegistry r){ this.reg=r; }

  @Override public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var timer = Timer
        .builder("cmd.latency").tag("cmd", cmd.getClass().getSimpleName()).register(reg);
    var sample = Timer.start(reg);
    return next.proceed(cmd, ctx).whenComplete((r,e) -> {
      sample.stop(timer);
      var c = Counter.builder("cmd.result")
             .tag("cmd", cmd.getClass().getSimpleName())
             .tag("status", e==null? "ok":"err").register(reg);
      c.increment();
    });
  }
}

