package com.marcusprado02.sharedkernel.cqrs.command;

import java.util.concurrent.CompletionStage;

/** Contrato de Handler fortemente tipado. */
public interface CommandHandler<C extends Command<R>, R> {
    Class<C> commandType();
    CompletionStage<R> handle(C command, CommandContext ctx) throws Exception;
}
