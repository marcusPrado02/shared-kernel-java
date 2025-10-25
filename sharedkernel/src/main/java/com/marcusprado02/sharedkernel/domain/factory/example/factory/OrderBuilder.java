package com.marcusprado02.sharedkernel.domain.factory.example.factory;

import java.util.*;

import com.marcusprado02.sharedkernel.domain.factory.BuilderFactory;
import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderItem;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

import static com.marcusprado02.sharedkernel.domain.model.base.Guard.notNull;
import static com.marcusprado02.sharedkernel.domain.model.base.Guard.that;

public final class OrderBuilder implements BuilderFactory<Order> {

    private final IdGenerator<OrderId> idGen;
    private final ClockProvider clock;
    private TenantId tenant;
    private String actor;
    private final List<OrderItem> items = new ArrayList<>();

    public OrderBuilder(IdGenerator<OrderId> idGen, ClockProvider clock) {
        this.idGen = idGen; this.clock = clock;
    }

    public OrderBuilder tenant(TenantId t) { this.tenant = notNull(t, "tenant"); return this; }
    public OrderBuilder actor(String a) { this.actor = notNull(a, "actor"); return this; }

    public OrderBuilder addItem(String sku, int qty, Money unitPrice) {
        that(qty > 0, () -> "qty>0");
        items.add(new OrderItem(new OrderItem.ItemId(idGen.newId().asString()+"-I"+(items.size()+1)), sku, qty, unitPrice));
        return this;
    }

    @Override
    public Order build() {
        notNull(tenant, "tenant");
        notNull(actor, "actor");
        var order = Order.createNew(idGen, tenant, actor);
        for (var it : items) order.addItem(it, actor);
        // validações finais já rodam dentro do AR via mutate/validateAggregate
        return order;
    }
}
