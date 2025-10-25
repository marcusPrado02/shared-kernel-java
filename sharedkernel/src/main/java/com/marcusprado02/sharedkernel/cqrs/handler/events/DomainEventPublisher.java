package com.marcusprado02.sharedkernel.cqrs.handler.events;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<Object> domainEvents); // eventos do domínio (tipos puros)
}
