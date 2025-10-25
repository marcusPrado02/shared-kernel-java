package com.marcusprado02.sharedkernel.domain.factory.example.factory;


import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.Random;

import com.marcusprado02.sharedkernel.domain.factory.TestDataFactory;
import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.base.UuidGenerator;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class OrderTestDataFactory implements TestDataFactory<Order> {

    private final OrderFactory factory;
    private final IdGenerator<OrderId> idGen;
    private final Random rnd;
    private final TenantId tenant;
    private final String actor;

    public OrderTestDataFactory(long seed) {
        this.rnd = new Random(seed);
        var fixedClock = ClockProvider.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        this.idGen = new UuidGenerator<>(OrderId::new);
        this.factory = new OrderFactory(idGen, fixedClock);
        this.tenant = new TenantId("acme");
        this.actor = "test:fixture";
    }

    @Override
    public Order get() {
        // SKU aleatório determinístico
        var sku = "SKU-" + (1000 + rnd.nextInt(9000));
        var price = Money.of(10 + rnd.nextInt(90), Currency.getInstance("USD").getCurrencyCode());
        var order = factory.createWithItem(tenant, sku, 1 + rnd.nextInt(3), Currency.getInstance("USD"), price.amount().doubleValue(), actor);
        return order;
    }

    public Order anyWithTwoItems() {
        var ob = new OrderBuilder(idGen, ClockProvider.systemUTC())
                .tenant(tenant).actor(actor)
                .addItem("SKU-AAA", 2, Money.of(19.90, "USD"))
                .addItem("SKU-BBB", 1, Money.of(70.00, "USD"));
        return ob.build();
    }
}
