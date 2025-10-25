package com.marcusprado02.sharedkernel.domain.model.example.entity;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public record LineAdjustment(
    String sku, String type, String description, Money amount
) {}

