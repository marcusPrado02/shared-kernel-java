package com.marcusprado02.sharedkernel.domain.factory.example.factory;

import java.time.LocalDate;
import java.util.Currency;
import java.util.Objects;

import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Order;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.OrderItem;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class OrderFactory {

    private final IdGenerator<OrderId> idGen;
    private final ClockProvider clock;

    public OrderFactory(IdGenerator<OrderId> idGen, ClockProvider clock) {
        this.idGen = Objects.requireNonNull(idGen);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Caso de uso canônico: novo pedido em DRAFT, sem itens. */
    public Order createDraft(TenantId tenant, String actor) {
        var order = Order.createNew(idGen, tenant, actor);
        // se quiser, publique evento “order.created.draft” no próprio AR
        return order;
    }

    /** Caso de uso: pedido já com um item inicial. */
    public Order createWithItem(TenantId tenant, String sku, int qty, Currency ccy, double unitPrice, String actor) {
        var order = createDraft(tenant, actor);
        var price = Money.of(unitPrice, ccy.getCurrencyCode());
        var item = new OrderItem(new OrderItem.ItemId(idGen.newId().asString()+"-I1"), sku, qty, price);
        order.addItem(item, actor);
        return order;
    }
}

