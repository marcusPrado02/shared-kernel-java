package com.marcusprado02.sharedkernel.infrastructure.inbox;

/** Resolve Handler para um tópico/DTO. */
public interface HandlerRegistry {
  <T> MessageHandler<T> resolve(String topic, Class<T> dtoClass);
}
