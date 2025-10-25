package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.concurrent.CompletionStage;

public interface QueryHandler<Q extends Query<R>, R> {
    Class<Q> queryType();
    CompletionStage<R> handle(Q query, QueryMetadata md) throws Exception;
}
