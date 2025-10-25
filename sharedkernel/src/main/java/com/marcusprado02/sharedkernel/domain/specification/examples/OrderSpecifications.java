package com.marcusprado02.sharedkernel.domain.specification.examples;

import java.math.BigDecimal;
import java.time.Instant;

import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.repository.Specification;

public final class OrderSpecifications {

    public static Specification<Order> statusIs(Order.Status status) {
        return o -> o.status() == status;
    }

    public static Specification<Order> createdBetween(Instant from, Instant to) {
        return o -> !o.createdAt().isBefore(from) && !o.createdAt().isAfter(to);
    }

    public static Specification<Order> totalGreaterThan(BigDecimal amount) {
        return o -> o.total().amount().compareTo(amount) > 0;
    }
}
