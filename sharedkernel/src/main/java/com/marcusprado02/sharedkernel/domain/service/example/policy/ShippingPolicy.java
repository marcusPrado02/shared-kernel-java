package com.marcusprado02.sharedkernel.domain.service.example.policy;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public interface ShippingPolicy {
    Money shippingFor(int totalItems, Money subtotal, Context ctx);
    record Context(String zip, boolean expedited) {}
}
