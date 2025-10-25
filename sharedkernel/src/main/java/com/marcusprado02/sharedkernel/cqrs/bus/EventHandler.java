package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.concurrent.CompletionStage;

public interface EventHandler<E> {
  Class<E> eventType();
  CompletionStage<Void> on(E event, EventContext ctx) throws Exception;
}