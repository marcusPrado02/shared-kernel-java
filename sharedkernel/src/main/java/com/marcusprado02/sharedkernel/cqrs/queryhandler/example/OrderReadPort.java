package com.marcusprado02.sharedkernel.cqrs.queryhandler.example;

import com.marcusprado02.sharedkernel.cqrs.query.*;
import com.marcusprado02.sharedkernel.cqrs.query.example.impl.GetOrdersByCustomer;

import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface OrderReadPort {
    Page<GetOrdersByCustomer.OrderSummary> findByCustomer(
        String customerId, int page, int size, String sortField, Sort.Direction dir,
        Optional<Instant> after, Optional<Instant> before,
        Optional<String> projection
    );
    List<String> allowedSortFields();
    List<String> allowedProjections(); // e.g. ["OrderSummaryV1","OrderSummaryWithItemsV1"]
}
