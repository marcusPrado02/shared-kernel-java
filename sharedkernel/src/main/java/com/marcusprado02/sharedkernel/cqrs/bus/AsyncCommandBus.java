package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.concurrent.CompletionStage;
import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;

public interface AsyncCommandBus {
  <C extends Command<R>, R> CompletionStage<R> send(C command, CommandContext ctx) throws Exception;
}
