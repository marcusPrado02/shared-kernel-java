package com.marcusprado02.sharedkernel.domain.service.example.policy;

import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public interface TaxPolicy {
    Money taxFor(String sku, Money lineTotal, Context ctx);
    record Context(String country, String state, String city) {}
}

