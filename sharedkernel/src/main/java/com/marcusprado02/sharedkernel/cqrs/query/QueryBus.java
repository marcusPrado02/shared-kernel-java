package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public interface QueryBus {
    <R, Q extends Query<R>> CompletionStage<QueryResult<R>> ask(Q query, Consumer<QueryMetadata.Builder> meta);
    default <R, Q extends Query<R>> CompletionStage<QueryResult<R>> ask(Q query){ return ask(query, b -> {}); }
}
