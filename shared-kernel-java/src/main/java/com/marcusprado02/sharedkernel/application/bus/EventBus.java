package com.marcusprado02.sharedkernel.application.bus;


import com.marcusprado02.sharedkernel.domain.event.DomainEvent;

/**
 * Publica DomainEvent para todos os listeners interessados.
 */
public interface EventBus {
    void publish(DomainEvent event);

    void publishAll(Iterable<? extends DomainEvent> events);
}
