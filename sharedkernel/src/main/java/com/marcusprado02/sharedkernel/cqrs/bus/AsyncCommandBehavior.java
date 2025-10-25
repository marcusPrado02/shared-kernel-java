package com.marcusprado02.sharedkernel.cqrs.bus;


import java.util.concurrent.CompletionStage;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

@FunctionalInterface
public interface AsyncCommandBehavior {
  <C extends Command<R>, R> CompletionStage<R> handle(C cmd, CommandContext ctx, Next<C,R> next);
  interface Next<C,R> { CompletionStage<R> proceed(C cmd, CommandContext ctx); }
}