package com.marcusprado02.sharedkernel.cqrs.bus;


import java.util.*;
import java.util.concurrent.*;
import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.handler.CommandHandler;

public final class PipelineAsyncCommandBus implements AsyncCommandBus {
  private final Map<Class<?>, CommandHandler<?,?>> registry = new ConcurrentHashMap<>();
  private final List<AsyncCommandBehavior> behaviors;

  public PipelineAsyncCommandBus(List<AsyncCommandBehavior> behaviors) {
    this.behaviors = List.copyOf(behaviors);
  }

  public <C extends Command<R>, R> void register(CommandHandler<C,R> handler) {
    registry.put(handler.commandType(), handler);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C extends Command<R>, R> CompletionStage<R> send(C command, CommandContext ctx) throws Exception {
    var h = (CommandHandler<C,R>) registry.get(command.getClass());
    if (h == null) return CompletableFuture.failedStage(new IllegalStateException("No handler for " + command.getClass()));

    AsyncCommandBehavior.Next<C,R> terminal = (cmd, cctx) -> {
      try {
        return h.handle(cmd, cctx);
      } catch (Exception e) {
        return CompletableFuture.failedStage(e);
      }
    };

    var next = terminal;
    for (int i = behaviors.size()-1; i >= 0; i--) {
      var current = behaviors.get(i);
      var prev = next;
      next = (cmd, cctx) -> current.handle(cmd, cctx, prev);
    }
    return next.proceed(command, ctx);
  }
}

