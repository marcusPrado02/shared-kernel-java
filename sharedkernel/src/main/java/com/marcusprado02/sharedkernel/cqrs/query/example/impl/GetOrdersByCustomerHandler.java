package com.marcusprado02.sharedkernel.cqrs.query.example.impl;

import java.util.Optional;
import java.util.List;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.query.*;

public final class GetOrdersByCustomerHandler implements QueryHandler<GetOrdersByCustomer, GetOrdersByCustomer.Result> {

    public interface OrderReadPort {
        Page<GetOrdersByCustomer.OrderSummary> findByCustomer(
                String customerId, int page, int size, String sortField, Sort.Direction dir,
                Optional<Instant> after, Optional<Instant> before,
                Optional<String> projection
        );
        static List<String> allowedSortFields() {
            return List.of("createdAt", "updatedAt", "status");
        }
    }

    private final OrderReadPort readPort;
    public GetOrdersByCustomerHandler(OrderReadPort port){ this.readPort = port; }

    @Override public Class<GetOrdersByCustomer> queryType() { return GetOrdersByCustomer.class; }

    @Override
    public CompletionStage<GetOrdersByCustomer.Result> handle(GetOrdersByCustomer q, QueryMetadata md) {
        // Whitelist de ordenação
        var sort = q.sort().orElse(new Sort("createdAt", Sort.Direction.DESC));
        if (!OrderReadPort.allowedSortFields().contains(sort.field()))
            throw new IllegalArgumentException("Campo de ordenação não permitido: " + sort.field());

        var page = readPort.findByCustomer(q.customerId(), q.page(), q.size(), sort.field(), sort.direction(),
                q.createdAfter(), q.createdBefore(), q.projectionName());

        return CompletableFuture.completedFuture(new GetOrdersByCustomer.Result(page));
    }
}
