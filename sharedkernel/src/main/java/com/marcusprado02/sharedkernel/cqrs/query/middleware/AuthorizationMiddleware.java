package com.marcusprado02.sharedkernel.cqrs.query.middleware;


import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.query.*;

public final class AuthorizationMiddleware implements QueryMiddleware {
    public interface Authorizer { boolean hasAll(String userId, Set<String> permissions); }
    private final Authorizer auth; private final java.util.function.Function<Query<?>, Set<String>> perms;
    public AuthorizationMiddleware(Authorizer a, java.util.function.Function<Query<?>, Set<String>> p){ this.auth=a; this.perms=p; }

    @Override public <R> java.util.concurrent.CompletionStage<QueryResult<R>> invoke(QueryEnvelope<R> env, Next next) {
        var required = perms.apply(env.query());
        if (required.isEmpty()) return next.invoke(env);
        var userId = env.metadata().userId();
        if (userId == null || !auth.hasAll(userId, required))
            return CompletableFuture.failedFuture(new SecurityException("Permissões insuficientes: " + required));
        return next.invoke(env);
    }
}