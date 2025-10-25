package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class IdempotencyMiddleware implements CommandMiddleware {
    public interface IdempotencyStore { boolean seen(String key); void record(String key); }
    private final IdempotencyStore store;

    public IdempotencyMiddleware(IdempotencyStore store){ this.store = store; }

    @Override public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var key = env.metadata().idempotencyKey;
        if (key == null || key.isBlank()) return next.invoke(env);
        if (store.seen(key)) return CompletableFuture.completedFuture(CommandResult.accepted());
        return next.invoke(env).whenComplete((r, t) -> { if (t==null) store.record(key); });
    }
}