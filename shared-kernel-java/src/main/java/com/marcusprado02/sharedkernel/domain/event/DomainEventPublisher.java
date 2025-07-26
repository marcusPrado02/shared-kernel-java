package com.marcusprado02.sharedkernel.domain.event;

public interface DomainEventPublisher {
    void publish(Object event);

    <R> R requestReply(Object event);
}
