package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class RetryMiddleware implements CommandMiddleware {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Override public <R> CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var policy = env.metadata().retryPolicy();
        if (policy.maxAttempts() <= 1) return next.invoke(env);

        CompletableFuture<CommandResult<R>> promise = new CompletableFuture<>();
        attempt(1, policy, env, next, promise);
        return promise;
    }

    private <R> void attempt(int attempt, RetryPolicy p, CommandEnvelope<R> env, Next next, CompletableFuture<CommandResult<R>> out) {
        next.invoke(env).whenComplete((res, err) -> {
            boolean retry = err != null || res.status() == CommandResult.Status.FAILED;
            if (!retry || attempt >= p.maxAttempts()) {
                if (err != null) out.complete(CommandResult.failed(err));
                else out.complete(res);
                return;
            }
            long delay = Math.round(p.initialBackoff().toMillis() * Math.pow(p.backoffMultiplier(), attempt-1));
            if (p.jitter()) delay = (long)(delay * (0.5 + Math.random()));
            scheduler.schedule(() -> attempt(attempt+1, p, env, next, out), delay, java.util.concurrent.TimeUnit.MILLISECONDS);
        });
    }
}
