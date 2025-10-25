package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.command.spi.OutboxWriter;

/**
 * Registra um “rastro” do Command no Outbox para auditoria/CDC (opcional).
 * Útil quando deseja correlacionar Commands com eventos de integração.
 */
public final class OutboxMiddleware implements CommandMiddleware {
    private final OutboxWriter outbox;
    private final java.util.function.Function<CommandEnvelope<?>, Map<String, Object>> mapper;

    public OutboxMiddleware(OutboxWriter outbox,
                            java.util.function.Function<CommandEnvelope<?>, Map<String, Object>> mapper) {
        this.outbox = outbox; this.mapper = mapper;
    }

    @Override
    public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        return next.invoke(env).whenComplete((res, err) -> {
            // Escreve Outbox somente em COMPLETED/ACCEPTED (configure como quiser)
            if (err == null && (res.status() == CommandResult.Status.COMPLETED || res.status() == CommandResult.Status.ACCEPTED)) {
                var payload = mapper.apply(env);
                outbox.append(
                    "command."+env.command().getClass().getSimpleName()+".completed",
                    env.metadata().commandId().value(),
                    payload,
                    env.metadata().tenantId,
                    env.metadata().traceparent,
                    env.metadata().correlationId,
                    env.metadata().causationId
                );
            }
        });
    }
}

