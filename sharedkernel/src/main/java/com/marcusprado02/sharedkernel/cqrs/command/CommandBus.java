package com.marcusprado02.sharedkernel.cqrs.command;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import com.marcusprado02.sharedkernel.adapters.out.messaging.MessageEnvelope;

/** Barramento de comandos, com suporte a middlewares. */
public interface CommandBus {
    <R, C extends Command<R>> CompletionStage<CommandResult<R>> dispatch(C command, Consumer<CommandMetadata.Builder> customizeMeta);
    default <R, C extends Command<R>> CompletionStage<CommandResult<R>> dispatch(C command) {
        return dispatch(command, b -> {});
    }
    default <R, C extends Command<R>> java.util.concurrent.CompletionStage<CommandResult<R>>
    dispatch(MessageEnvelope<? extends Command> env) {
    return dispatch(env.payload(), meta -> {
        meta.tenantId(env.tenantId());
        meta.correlationId(env.correlationId());
        meta.causationId(env.causationId());
        env.tags().forEach(t -> meta.tag(t));
        env.headers().forEach((k,v) -> meta.header(k,v));
    });
    }
}