package com.marcusprado02.sharedkernel.domain.repository.example.repository;


import java.time.Instant;
import java.util.Optional;

import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.repository.AggregateRepository;
import com.marcusprado02.sharedkernel.domain.repository.PageRequest;
import com.marcusprado02.sharedkernel.domain.repository.PageResult;
import com.marcusprado02.sharedkernel.domain.repository.Specification;

public interface OrderRepository extends AggregateRepository<Order, OrderId> {

    /** Consultas específicas: */
    PageResult<Order> findByCustomer(String customerId, PageRequest page);

    PageResult<Order> findByStatus(Order.Status status, PageRequest page);

    Optional<Order> findByExternalRef(String externalRef);

    /** Exemplos de specs reutilizáveis: */
    static Specification<Order> createdBetween(Instant from, Instant to) {
        return o -> !o.createdAt().isBefore(from) && !o.createdAt().isAfter(to);
    }

    static Specification<Order> statusIn(Order.Status... statuses) {
        var set = java.util.Set.of(statuses);
        return o -> set.contains(o.status());
    }
}
