package com.marcusprado02.sharedkernel.domain.factory.example.factory;


import java.time.LocalDate;
import java.util.Objects;

import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.example.entity.Subscription;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class SubscriptionFactory {

    private final IdGenerator<Subscription.SubscriptionId> idGen;
    private final ClockProvider clock;

    public SubscriptionFactory(IdGenerator<Subscription.SubscriptionId> idGen, ClockProvider clock) {
        this.idGen = Objects.requireNonNull(idGen);
        this.clock = Objects.requireNonNull(clock);
    }

    public Subscription createNew(String planCode, TenantId tenant, String actor) {
        var today = LocalDate.now(clock.clock());
        return Subscription.createNew(idGen, planCode, today, tenant, actor);
    }
}
