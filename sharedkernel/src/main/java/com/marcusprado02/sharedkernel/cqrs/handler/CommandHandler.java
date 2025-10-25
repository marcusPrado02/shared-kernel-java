package com.marcusprado02.sharedkernel.cqrs.handler;

import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

public interface CommandHandler<C extends Command<R>, R> {
    Class<C> commandType();
    CompletionStage<R> handle(C command, CommandContext ctx) throws Exception;
}