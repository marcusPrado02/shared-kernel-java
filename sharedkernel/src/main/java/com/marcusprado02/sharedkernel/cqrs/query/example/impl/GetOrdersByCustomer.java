package com.marcusprado02.sharedkernel.cqrs.query.example.impl;

import java.time.Instant;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

import com.marcusprado02.sharedkernel.cqrs.query.*;

public record GetOrdersByCustomer(
        String customerId,
        Optional<Instant> createdAfter,
        Optional<Instant> createdBefore,
        int page, int size,
        Optional<Sort> sort,                // e.g. ("createdAt", DESC)
        Optional<String> projectionName     // e.g. "OrderSummaryV1"
) implements Query<GetOrdersByCustomer.Result> {

    public record OrderSummary(String orderId, Instant createdAt, String status, BigDecimal total) {}
    public record Result(Page<OrderSummary> page) {}
}
