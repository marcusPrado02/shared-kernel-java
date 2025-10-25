package com.marcusprado02.sharedkernel.cqrs.handler.example;


import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import com.marcusprado02.sharedkernel.cqrs.command.*;
import com.marcusprado02.sharedkernel.cqrs.command.example.impl.*;
import com.marcusprado02.sharedkernel.cqrs.handler.*;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;

public final class CreateOrderHandler extends AbstractCommandHandler<CreateOrderCommand, CreateOrderCommand.CreateOrderResult> {

    private final OrderRepository orders;

    public CreateOrderHandler(Builder<CreateOrderCommand, CreateOrderCommand.CreateOrderResult> b, OrderRepository orders) {
        super(b);
        this.orders = orders;
    }

    @Override public Class<CreateOrderCommand> commandType() { return CreateOrderCommand.class; }

    @Override
    protected CreateOrderCommand.CreateOrderResult doHandle(CreateOrderCommand cmd, CommandContext ctx) {
        // Regra defensiva 1: idempotência natural por chave de negócio
        if (orders.exists(cmd.orderId())) {
            return new CreateOrderCommand.CreateOrderResult(cmd.orderId(), "ALREADY_EXISTS");
        }

        // Regra defensiva 2: soma total coerente
        var total = cmd.lines().stream()
                .map(l -> l.unitPrice().multiply(BigDecimal.valueOf(l.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cmd.expectedTotal() != null && total.compareTo(cmd.expectedTotal()) != 0) {
            throw new IllegalArgumentException("Total divergente");
        }

        // Construção do Aggregate
        var aggLines = cmd.lines().stream()
                .map(l -> new Order.Line(l.sku(), l.qty(), l.unitPrice()))
                .toList();
        var order = new Order(cmd.orderId(), cmd.customerId(), aggLines);

        // Persistência
        orders.save(order);

        return new CreateOrderCommand.CreateOrderResult(order.id().toString(), "CREATED");
    }

    @Override
    protected Set<String> requiredPermissions(CreateOrderCommand cmd) {
        return Set.of("order:create");
    }

    @Override
    protected List<Object> collectDomainEventsToPublish(CreateOrderCommand cmd, CommandContext ctx) {
        var o = orders.findById(cmd.orderId());
        return orders.pullDomainEventsAndClear(o);
    }

    @Override
    protected List<OutboxMessage> toOutboxMessages(CreateOrderCommand cmd, CommandContext ctx) {
        // Exemplo: publicar evento de integração “OrderCreatedV1”
        var payload = java.util.Map.of(
            "orderId", cmd.orderId(),
            "customerId", cmd.customerId(),
            "ts", ctx.now().toString()
        );
        return List.of(new OutboxMessage("order", "order-created-" + cmd.orderId(), payload));
    }
}
