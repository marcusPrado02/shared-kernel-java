package com.marcusprado02.sharedkernel.cqrs.command.example.impl;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

import com.marcusprado02.sharedkernel.cqrs.command.Command;
import com.marcusprado02.sharedkernel.cqrs.command.security.RequiresPermission;
import com.marcusprado02.sharedkernel.cqrs.command.example.impl.CreateOrderCommand.CreateOrderResult;

@RequiresPermission({"order:create"})
public record CreateOrderCommand(
        @NotNull String orderId,
        @NotNull String customerId,
        @Size(min = 1) List<Line> lines,
        @DecimalMin("0.00") BigDecimal expectedTotal // proteção contra tampering
) implements Command<CreateOrderResult> {

    public record Line(@NotNull String sku, @Min(1) int qty, @DecimalMin("0.00") BigDecimal unitPrice) {}
    public record CreateOrderResult(String orderId, String status) {}
}
