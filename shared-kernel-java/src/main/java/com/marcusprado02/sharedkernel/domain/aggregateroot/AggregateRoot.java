package com.marcusprado02.sharedkernel.domain.aggregateroot;

import java.util.List;
import com.marcusprado02.sharedkernel.domain.event.DomainEvent;

/**
 * Marca um Aggregate Root em DDD.
 */
public interface AggregateRoot<ID> {
    ID getId();

    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
