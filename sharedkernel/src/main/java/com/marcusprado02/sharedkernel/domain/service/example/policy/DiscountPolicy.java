package com.marcusprado02.sharedkernel.domain.service.example.policy;

import java.util.List;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public interface DiscountPolicy {
    Money discountFor(String sku, int quantity, Money unitPrice, Context ctx);

    record Context(String customerTier, boolean firstPurchase, List<String> activeCoupons) {}
}

