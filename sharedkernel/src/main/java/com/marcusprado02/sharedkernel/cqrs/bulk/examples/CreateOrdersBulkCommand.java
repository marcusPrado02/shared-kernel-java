package com.marcusprado02.sharedkernel.cqrs.bulk.examples;


import java.util.List;

import com.marcusprado02.sharedkernel.cqrs.bulk.BulkPolicy;
import com.marcusprado02.sharedkernel.cqrs.command.example.impl.CreateOrderCommand;
import com.marcusprado02.sharedkernel.cqrs.bulk.HomogeneousBulkCommand;

public final class CreateOrdersBulkCommand extends HomogeneousBulkCommand<CreateOrderCommand, CreateOrderCommand.CreateOrderResult> {
    public CreateOrdersBulkCommand(List<CreateOrderCommand> items, BulkPolicy policy) { super(items, policy); }
    public static CreateOrdersBulkCommand balanced(List<CreateOrderCommand> items) { return new CreateOrdersBulkCommand(items, BulkPolicy.balanced()); }
}

