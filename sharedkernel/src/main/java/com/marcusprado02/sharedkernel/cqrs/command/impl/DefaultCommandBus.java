package com.marcusprado02.sharedkernel.cqrs.command.impl;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandBus;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;
import com.marcusprado02.sharedkernel.cqrs.command.CommandEnvelope;
import com.marcusprado02.sharedkernel.cqrs.command.CommandHandler;
import com.marcusprado02.sharedkernel.cqrs.command.CommandMetadata;
import com.marcusprado02.sharedkernel.cqrs.command.CommandResult;
import com.marcusprado02.sharedkernel.cqrs.command.middleware.CommandMiddleware;
import java.util.function.Consumer;

/** Implementação padrão do CommandBus com pipeline. */
public final class DefaultCommandBus implements CommandBus {
    private final Map<Class<?>, CommandHandler<?,?>> handlers;
    private final List<CommandMiddleware> middlewares;

    public DefaultCommandBus(Collection<CommandHandler<?,?>> handlers, List<CommandMiddleware> middlewares) {
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(CommandHandler::commandType, Function.identity()));
        this.middlewares = List.copyOf(middlewares);
    }

    @SuppressWarnings("unchecked")
    @Override public <R, C extends Command<R>> CompletionStage<CommandResult<R>> dispatch(C command, Consumer<CommandMetadata.Builder> customizeMeta) {
        var meta = CommandMetadata.builder(); customizeMeta.accept(meta);
        var env = new CommandEnvelope<>(command, meta.build());

        // Resolve Handler
        var handler = (CommandHandler<C,R>) Optional.ofNullable(handlers.get(command.getClass()))
                .orElseThrow(() -> new IllegalStateException("Handler não encontrado para " + command.getClass().getName()));

        // Monta cadeia de middlewares
        CommandMiddleware.Next terminal = new CommandMiddleware.Next(){
            @Override public <T> CompletionStage<CommandResult<T>> invoke(CommandEnvelope<T> e) {
                var ctx = new SimpleCommandContext(e.metadata());
                try {
                    CompletableFuture<CommandResult<T>> future = new CompletableFuture<>();
                    handler.handle((C) e.command(), ctx)
                        .whenComplete((res, ex) -> {
                            if (ex != null) {
                                future.complete(CommandResult.failed(ex));
                            } else {
                                @SuppressWarnings("unchecked")
                                CommandResult<T> cr = CommandResult.completed((T) res);
                                future.complete(cr);
                            }
                        });
                    return future;
                } catch (Exception ex) {
                    CompletableFuture<CommandResult<T>> f = new CompletableFuture<>();
                    f.complete(CommandResult.failed(ex));
                    return f;
                }
            }
        };
        var chain = buildChain(terminal, middlewares.iterator());
        return chain.invoke((CommandEnvelope<R>) env);
    }

    private CommandMiddleware.Next buildChain(CommandMiddleware.Next terminal, Iterator<CommandMiddleware> it){
        if (!it.hasNext()) return terminal;
        var mw = it.next();
        var next = buildChain(terminal, it);
        return new CommandMiddleware.Next(){
            @Override public <R> CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env) {
                return mw.invoke(env, next);
            }
        };
    }

    /** Contexto mínimo padrão. */
    private record SimpleCommandContext(CommandMetadata md) implements CommandContext {
        @Override public Instant now(){ return md.timestampUtc; }
        @Override public Optional<String> tenantId(){ return Optional.ofNullable(md.tenantId); }
        @Override public Optional<String> userId(){ return Optional.ofNullable(md.userId); }
        @Override public Optional<String> traceparent(){ return Optional.ofNullable(md.traceparent); }
        @Override public <T> Optional<T> attribute(String key, Class<T> type){ 
            Object v = md.attributes != null ? md.attributes.get(key) : null;
            if (v == null) return Optional.empty();
            if (!type.isInstance(v)) throw new IllegalArgumentException("Attribute "+key+" is not of type "+type.getName());
            return Optional.of(type.cast(v));
        }
        @Override
        public Map<String, Object> headers() {
            return md.headers == null ? Map.of() : Map.copyOf(md.headers);
        }
        @Override
        public Optional<String> correlationId() {
            return Optional.ofNullable(md.correlationId);
        }
    }
}
