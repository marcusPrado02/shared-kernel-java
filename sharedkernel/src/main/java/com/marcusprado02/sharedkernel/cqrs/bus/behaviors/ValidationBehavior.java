package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolationException;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class ValidationBehavior implements AsyncCommandBehavior {
  private final Validator validator;

  public ValidationBehavior(Validator validator) { this.validator = validator; }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var v = validator.validate(cmd);
    if (!v.isEmpty()) return CompletableFuture.failedStage(new ConstraintViolationException(v));
    return next.proceed(cmd, ctx);
  }
}
