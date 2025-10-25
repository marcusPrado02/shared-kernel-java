package com.marcusprado02.sharedkernel.cqrs.bulk;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;
import com.marcusprado02.sharedkernel.cqrs.handler.CommandHandler;

public final class BulkCommandHandler<R> implements CommandHandler<BulkCommand<R>, BulkResult<R>> {
    private final BulkExecutor executor;
    public BulkCommandHandler(BulkExecutor executor){ this.executor = executor; }

    @Override public Class<BulkCommand<R>> commandType() { @SuppressWarnings("unchecked") var cls = (Class<BulkCommand<R>>) (Class<?>) BulkCommand.class; return cls; }

    @Override public CompletionStage<BulkResult<R>> handle(BulkCommand<R> command, CommandContext ctx) {
        // Propaga metadados compartilhados (tenant, user, correlation, etc.)
        return executor.execute(command, b -> {
            ctx.tenantId().ifPresent(b::tenantId);
            ctx.userId().ifPresent(b::userId);
            ctx.traceparent().ifPresent(b::traceparent);
        });
    }
}
