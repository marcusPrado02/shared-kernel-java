package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.concurrent.CompletionStage;

public interface EventBus {
  <E> CompletionStage<Void> publish(E event, EventContext ctx);
  <E> void register(EventHandler<E> h);
}

