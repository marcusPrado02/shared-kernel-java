package com.marcusprado02.sharedkernel.saga.example.Impl;

import java.math.BigDecimal;
import java.util.Map;

import com.marcusprado02.sharedkernel.cqrs.command.Command;

public class Commands {
    public record ReserveInventoryCmd(String orderId, Map<String,Integer> items) implements Command {}
    public record ChargePaymentCmd(String orderId, BigDecimal amount) implements Command {}
    public record ArrangeShippingCmd(String orderId) implements Command {}
    public record ReleaseInventoryCmd(String orderId) implements Command {}
    public record RefundPaymentCmd(String orderId, BigDecimal amount) implements Command {}
}
