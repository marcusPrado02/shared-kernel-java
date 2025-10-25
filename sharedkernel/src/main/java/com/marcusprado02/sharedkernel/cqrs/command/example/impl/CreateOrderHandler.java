package com.marcusprado02.sharedkernel.cqrs.command.example.impl;


import java.math.BigDecimal;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.cqrs.command.CommandContext;
import com.marcusprado02.sharedkernel.cqrs.command.CommandHandler;

public final class CreateOrderHandler implements CommandHandler<CreateOrderCommand, CreateOrderCommand.CreateOrderResult> {
    @Override public Class<CreateOrderCommand> commandType() { return CreateOrderCommand.class; }

    @Override
    public CompletionStage<CreateOrderCommand.CreateOrderResult> handle(CreateOrderCommand cmd, CommandContext ctx) {
        // Exemplo de regra de negócio defensiva
        var total = cmd.lines().stream()
                .map(l -> l.unitPrice().multiply(BigDecimal.valueOf(l.qty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (cmd.expectedTotal() != null && total.compareTo(cmd.expectedTotal()) != 0) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Total divergente"));
        }

        // Chamar Domain Service / Repository / emitir eventos etc.
        // orderRepository.save(...); eventBus.publish(new OrderCreated(...));
        var result = new CreateOrderCommand.CreateOrderResult(cmd.orderId(), "CREATED");
        return CompletableFuture.completedFuture(result);
    }
}
