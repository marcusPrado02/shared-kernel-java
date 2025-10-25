package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.concurrent.CompletionStage;
import java.util.function.UnaryOperator;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class InputSanitizerBehavior implements AsyncCommandBehavior {
  private final UnaryOperator<Object> sanitizer; // aplica no próprio comando

  public InputSanitizerBehavior(UnaryOperator<Object> sanitizer) { this.sanitizer = sanitizer; }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var clean = (C) sanitizer.apply(cmd);
    return next.proceed(clean, ctx);
  }
}