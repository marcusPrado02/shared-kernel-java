package com.marcusprado02.sharedkernel.domain.model.example.entity;


import java.time.Instant;

import com.marcusprado02.sharedkernel.domain.model.base.BaseIdentifier;
import com.marcusprado02.sharedkernel.domain.model.base.Entity;
import com.marcusprado02.sharedkernel.domain.model.value.money.Money;

import static com.marcusprado02.sharedkernel.domain.model.base.Guard.notNull;
import static com.marcusprado02.sharedkernel.domain.model.base.Guard.that;


public final class OrderItem extends Entity<OrderItem.ItemId> {

    public static final class ItemId extends BaseIdentifier<String> {
        public ItemId(String value) { super(value); }
    }

    private String sku;
    private int quantity;
    private Money unitPrice;

    public OrderItem(ItemId id, String sku, int quantity, Money unitPrice) {
        super(notNull(id, "id"));
        this.sku = notNull(sku, "sku");
        that(quantity > 0, () -> "quantity must be > 0");
        this.quantity = quantity;
        this.unitPrice = notNull(unitPrice, "unitPrice");
    }

    public Money lineTotal() { return unitPrice.times(java.math.BigDecimal.valueOf(quantity)); }

    // getters restritos (sem setters públicos)
    public String sku() { return sku; }
    public int quantity() { return quantity; }
    public Money unitPrice() { return unitPrice; }

    // mutações internas controladas pelo AR:
    void increase(int delta) { that(delta > 0, () -> "delta>0"); this.quantity += delta; touch(); }
    void decrease(int delta) { that(delta > 0 && delta <= quantity, () -> "invalid delta"); this.quantity -= delta; touch(); }
}
