package com.marcusprado02.sharedkernel.cqrs.queryhandler.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.marcusprado02.sharedkernel.cqrs.query.*;
import com.marcusprado02.sharedkernel.cqrs.query.example.impl.GetOrdersByCustomer;
import com.marcusprado02.sharedkernel.cqrs.queryhandler.AbstractQueryHandler;

public final class GetOrdersByCustomerHandler
        extends AbstractQueryHandler<GetOrdersByCustomer, GetOrdersByCustomer.Result> {

    private final OrderReadPort readPort;

    public GetOrdersByCustomerHandler(Builder<GetOrdersByCustomer, GetOrdersByCustomer.Result> b, OrderReadPort port) {
        super(b);
        this.readPort = port;
    }

    @Override public Class<GetOrdersByCustomer> queryType() { return GetOrdersByCustomer.class; }

    @Override protected java.util.Set<String> requiredPermissions(GetOrdersByCustomer q) {
        return java.util.Set.of("order:read");
    }

    @Override protected void validateSortProjection(GetOrdersByCustomer q, QueryMetadata md) {
        var sort = q.sort().orElse(new Sort("createdAt", Sort.Direction.DESC));
        if (!readPort.allowedSortFields().contains(sort.field()))
            throw new IllegalArgumentException("Campo de ordenação não permitido: " + sort.field());
        q.projectionName().ifPresent(p -> {
            if (!readPort.allowedProjections().contains(p))
                throw new IllegalArgumentException("Projeção não permitida: " + p);
        });
    }

    @Override
    protected CompletionStage<GetOrdersByCustomer.Result> doHandle(GetOrdersByCustomer q, QueryMetadata md) {
        var sort = q.sort().orElse(new Sort("createdAt", Sort.Direction.DESC));
        var page = readPort.findByCustomer(q.customerId(), q.page(), q.size(),
                sort.field(), sort.direction(), q.createdAfter(), q.createdBefore(), q.projectionName());
        return CompletableFuture.completedFuture(new GetOrdersByCustomer.Result(page));
    }
}
