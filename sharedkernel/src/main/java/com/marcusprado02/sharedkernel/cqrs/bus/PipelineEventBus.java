package com.marcusprado02.sharedkernel.cqrs.bus;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class PipelineEventBus implements EventBus {
  private final Map<Class<?>, List<EventHandler<?>>> reg = new ConcurrentHashMap<>();

  @Override public <E> CompletionStage<Void> publish(E event, EventContext ctx) {
    var handlers = (List<EventHandler<E>>) (List<?>) reg.getOrDefault(event.getClass(), List.of());
    var futures = handlers.stream().map(h -> {
      try { return h.on(event, ctx); } catch (Exception e) { return CompletableFuture.<Void>failedStage(e); }
    }).toList();
    var cfs = futures.stream().map(f -> {
      CompletableFuture<Void> cf = new CompletableFuture<>();
      f.whenComplete((r, ex) -> { if (ex != null) cf.completeExceptionally(ex); else cf.complete(null); });
      return cf;
    }).toArray(CompletableFuture[]::new);
    return CompletableFuture.allOf(cfs).thenApply(v -> null);
  }

  @Override public <E> void register(EventHandler<E> h) {
    reg.computeIfAbsent(h.eventType(), k -> new CopyOnWriteArrayList<>()).add(h);
  }
}
