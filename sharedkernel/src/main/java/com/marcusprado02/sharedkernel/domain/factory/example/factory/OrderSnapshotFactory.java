package com.marcusprado02.sharedkernel.domain.factory.example.factory;


import java.util.List;

import com.marcusprado02.sharedkernel.domain.factory.SnapshotFactory;
import com.marcusprado02.sharedkernel.domain.factory.example.factory.OrderSnapshotFactory.OrderSnapshot;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

public final class OrderSnapshotFactory implements SnapshotFactory<Order, OrderSnapshot> {

    @Override
    public OrderSnapshot snapshotOf(Order o) {
        var items = o.items().stream()
                .map(i -> new OrderSnapshot.Item(i.id().asString(), i.sku(), i.quantity(), i.unitPrice()))
                .toList();
        return new OrderSnapshot(o.id().asString(), o.status().name(), items, o.total());
    }

    /** DTO estável do domínio */
    public record OrderSnapshot(
            String id,
            String status,
            List<Item> items,
            Money total
    ) {
        public record Item(String id, String sku, int qty, Money unitPrice) {}
    }
}
