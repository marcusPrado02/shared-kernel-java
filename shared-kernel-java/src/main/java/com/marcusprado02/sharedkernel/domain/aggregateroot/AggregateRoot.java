package com.marcusprado02.sharedkernel.domain.aggregateroot;

import java.util.List;

/**
 * Marca um Aggregate Root em DDD.
 */
public interface AggregateRoot<ID> {
    ID getId();

    List<Object> getDomainEvents();

    void clearDomainEvents();
}
