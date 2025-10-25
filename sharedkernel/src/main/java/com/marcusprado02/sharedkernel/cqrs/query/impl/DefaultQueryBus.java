package com.marcusprado02.sharedkernel.cqrs.query.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.cqrs.query.*;
import com.marcusprado02.sharedkernel.cqrs.query.middleware.QueryMiddleware;

public final class DefaultQueryBus implements QueryBus {
    private final Map<Class<?>, QueryHandler<?,?>> handlers;
    private final List<QueryMiddleware> middlewares;

    public DefaultQueryBus(Collection<QueryHandler<?,?>> handlers, List<QueryMiddleware> middlewares) {
        this.handlers = handlers.stream().collect(Collectors.toUnmodifiableMap(QueryHandler::queryType, Function.identity()));
        this.middlewares = List.copyOf(middlewares);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, Q extends Query<R>> CompletionStage<QueryResult<R>> ask(Q query, java.util.function.Consumer<QueryMetadata.Builder> meta) {
        var md = QueryMetadata.builder(); meta.accept(md);
        var env = new QueryEnvelope<R>(query, md.build());

        // Handler correto para esta query concreta
        final QueryHandler<Q, R> handler = (QueryHandler<Q, R>) Optional.ofNullable(handlers.get(query.getClass()))
                .orElseThrow(() -> new IllegalStateException("QueryHandler não encontrado para " + query.getClass().getName()));

        // Terminal da cadeia: chama o handler e embrulha em QueryResult
        QueryMiddleware.Next terminal = new QueryMiddleware.Next() {
            @Override public <T> CompletionStage<QueryResult<T>> invoke(QueryEnvelope<T> e) {
                try {
                    // Aqui, T deve ser R porque chain.invoke receberá env tipado com R
                    return ((CompletionStage<T>) handler.handle((Q) e.query(), e.metadata()))
                            .thenApply(v -> QueryResult.<T>of(v)); // << type witness evita inferência para Object
                } catch (Exception ex) {
                    var cf = new CompletableFuture<QueryResult<T>>();
                    cf.completeExceptionally(ex);
                    return cf;
                }
            }
        };

        var chain = buildChain(terminal, middlewares.iterator());
        return chain.invoke((QueryEnvelope<R>) env);
    }

    private QueryMiddleware.Next buildChain(QueryMiddleware.Next terminal, Iterator<QueryMiddleware> it){
        if (!it.hasNext()) return terminal;
        var mw = it.next();
        var next = buildChain(terminal, it);

        // Não pode usar lambda porque Next#invoke é genérico
        return new QueryMiddleware.Next() {
            @Override
            public <R> CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env) {
                return mw.invoke(env, next);
            }
        };
    }
}