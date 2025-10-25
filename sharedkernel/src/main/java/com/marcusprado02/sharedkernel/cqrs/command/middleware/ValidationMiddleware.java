package com.marcusprado02.sharedkernel.cqrs.command.middleware;

import jakarta.validation.Validator;

import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.cqrs.command.*;

public final class ValidationMiddleware implements CommandMiddleware {
    private final Validator validator;
    public ValidationMiddleware(Validator validator){ this.validator = validator; }

    @Override public <R> java.util.concurrent.CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next) {
        var violations = validator.validate(env.command());
        if (!violations.isEmpty()) {
            var msg = violations.stream().map(v -> v.getPropertyPath()+": "+v.getMessage()).collect(Collectors.joining("; "));
            return java.util.concurrent.CompletableFuture.completedFuture(CommandResult.rejected("Validação falhou: " + msg));
        }
        return next.invoke(env);
    }
}
