package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.command.spi.TracerFacade;

public final class TracingMiddleware implements CommandMiddleware {
    private final TracerFacade tracer;

    public TracingMiddleware(TracerFacade tracer){ this.tracer = tracer; }

    @Override
    public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var span = tracer.startSpan("command.dispatch",
            TracerFacade.Tags.of()
                .put("cmd.name", env.command().getClass().getSimpleName())
                .put("cmd.id", env.metadata().commandId().value())
                .put("tenant", env.metadata().tenantId)
                .put("user", env.metadata().userId)
                .put("traceparent.in", env.metadata().traceparent)
        );
        try {
            // injeta traceparent no metadata (propagação downstream)
            var md = CommandMetadata.builder()
                    .commandId(env.metadata().commandId)
                    .correlationId(env.metadata().correlationId)
                    .causationId(env.metadata().causationId)
                    .idempotencyKey(env.metadata().idempotencyKey)
                    .tenantId(env.metadata().tenantId)
                    .userId(env.metadata().userId)
                    .timestampUtc(env.metadata().timestampUtc)
                    .executeAtUtc(env.metadata().executeAtUtc)
                    .priority(env.metadata().priority)
                    .retryPolicy(env.metadata().retryPolicy)
                    .traceparent(tracer.currentTraceparent())
                    .attributes(env.metadata().attributes)
                    .build();

            return next.invoke(new CommandEnvelope<>(env.command(), md))
                .whenComplete((res, err) -> {
                    if (err != null) {
                        tracer.tag(span, "error", "true");
                        tracer.tag(span, "exception", err.getClass().getSimpleName());
                        tracer.tag(span, "message", err.getMessage());
                    } else if (res != null) {
                        tracer.tag(span, "status", res.status().name());
                    }
                    tracer.endSpan(span);
                });
        } catch (Throwable t) {
            tracer.tag(span, "error", "true");
            tracer.endSpan(span);
            return CompletableFuture.completedFuture(CommandResult.failed(t));
        }
    }
}
