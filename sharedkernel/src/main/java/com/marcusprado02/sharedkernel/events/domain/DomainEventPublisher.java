package com.marcusprado02.sharedkernel.events.domain;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<? extends DomainEvent> events);
}
