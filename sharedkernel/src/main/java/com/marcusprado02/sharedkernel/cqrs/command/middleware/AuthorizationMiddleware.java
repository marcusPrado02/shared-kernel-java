package com.marcusprado02.sharedkernel.cqrs.command.middleware;


import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.command.security.RequiresPermission;

public final class AuthorizationMiddleware implements CommandMiddleware {
    public interface PermissionChecker {
        boolean hasAll(String userId, Set<String> required);
    }
    private final PermissionChecker checker;

    public AuthorizationMiddleware(PermissionChecker checker){ this.checker = checker; }

    @Override public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var anno = env.command().getClass().getAnnotation(RequiresPermission.class);
        if (anno == null) return next.invoke(env);
        var userId = env.metadata().userId;
        if (userId == null) return CompletableFuture.completedFuture(CommandResult.rejected("Usuário não autenticado"));
        var required = Set.of(anno.value());
        return checker.hasAll(userId, required)
                ? next.invoke(env)
                : CompletableFuture.completedFuture(CommandResult.rejected("Permissões insuficientes: " + required));
    }
}