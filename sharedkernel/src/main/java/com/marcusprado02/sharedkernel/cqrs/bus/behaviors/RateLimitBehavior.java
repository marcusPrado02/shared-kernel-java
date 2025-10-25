package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.cqrs.bus.AsyncCommandBehavior;
import com.marcusprado02.sharedkernel.cqrs.command.*;


public final class RateLimitBehavior implements AsyncCommandBehavior {
  private final BucketRegistry buckets;
  private final Function<CommandContext,String> keyFn;

  public RateLimitBehavior(BucketRegistry buckets, Function<CommandContext,String> keyFn) {
    this.buckets = buckets; this.keyFn = keyFn;
  }

  @Override
  public <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next) {
    var bucket = buckets.get(keyFn.apply(ctx));
    if (!bucket.tryConsume(1)) return CompletableFuture.failedStage(new TooManyRequestsException());
    return next.proceed(cmd, ctx);
  }

  public static final class TooManyRequestsException extends RuntimeException {}
}
