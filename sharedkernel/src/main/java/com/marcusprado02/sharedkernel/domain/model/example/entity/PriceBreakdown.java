package com.marcusprado02.sharedkernel.domain.model.example.entity;

import java.util.List;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public record PriceBreakdown(
    Money subtotal,
    Money totalDiscount,
    Money taxTotal,
    Money shipping,
    Money grandTotal,
    List<LineAdjustment> adjustments
) {}

