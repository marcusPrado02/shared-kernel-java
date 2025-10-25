package com.marcusprado02.sharedkernel.cqrs.command.middleware;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.command.CommandEnvelope;
import com.marcusprado02.sharedkernel.cqrs.command.CommandResult;

public interface CommandMiddleware {
    interface Next {
        <R> CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env);
    }
    <R> CompletionStage<CommandResult<R>> invoke(CommandEnvelope<R> env, Next next);
}
