package com.marcusprado02.sharedkernel.cqrs.query.middleware;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.query.*;

public interface QueryMiddleware {
    interface Next { <R> CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env); }
    <R> CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env, Next next);
}
