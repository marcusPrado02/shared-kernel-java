package com.marcusprado02.sharedkernel.domain.model.example.entity;

import java.time.Instant;
import java.time.LocalDate;

import com.marcusprado02.sharedkernel.domain.model.base.AggregateRoot;
import com.marcusprado02.sharedkernel.domain.model.base.BaseIdentifier;
import com.marcusprado02.sharedkernel.domain.model.base.IdGenerator;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.model.base.Version;

import static com.marcusprado02.sharedkernel.domain.model.base.Guard.notNull;
import static com.marcusprado02.sharedkernel.domain.model.base.Guard.that;

public final class Subscription extends AggregateRoot<Subscription.SubscriptionId> {

    // ---- ID forte específico da entidade ----
    public static final class SubscriptionId extends BaseIdentifier<String> {
        public SubscriptionId(String value) { super(value); }
    }

    // ---- Campos do domínio (essenciais) ----
    private String planCode;
    private LocalDate startDate;
    private LocalDate endDate;     // null = recorrente/indefinida
    private boolean active;

    // ==== Fábricas estáticas (criação canônica) ====
    public static Subscription createNew(IdGenerator<SubscriptionId> idGen,
                                         String planCode,
                                         LocalDate start,
                                         TenantId tenant,
                                         String actor) {
        notNull(idGen, "idGen");
        notNull(planCode, "planCode");
        notNull(start, "startDate");

        var s = new Subscription(idGen.newId());
        // Use o ciclo de mutação seguro do AggregateRoot:
        s.mutate(actor, () -> {
            s.planCode = planCode;
            s.startDate = start;
            s.active = true;
            s.setTenant(tenant); // `touch()` interno; auditoria é aplicada pelo mutate()
            // Evento de criação
            s.publishEvent(() -> new com.marcusprado02.sharedkernel.domain.model.example.events.SubscriptionCreated(
                s.id(), Instant.now()
            ));
        });
        return s;
    }

    /** Reconstituição (a partir do repositório). */
    public static Subscription reconstitute(SubscriptionId id, Version v, TenantId t,
                                            java.time.Instant c, java.time.Instant u,
                                            String cb, String ub, java.time.Instant d,
                                            String planCode, LocalDate startDate, LocalDate endDate, boolean active) {
        var s = new Subscription(id, v, t, c, u, cb, ub, d);
        s.planCode = planCode;
        s.startDate = startDate;
        s.endDate = endDate;
        s.active = active;
        // Garante invariantes do agregado mesmo na carga
        s.validateAggregate();
        return s;
    }

    private Subscription(SubscriptionId id) { super(id); }
    private Subscription(SubscriptionId id, Version v, TenantId t, java.time.Instant c, java.time.Instant u,
                         String cb, String ub, java.time.Instant d) {
        super(id, v, t, c, u, cb, ub, d);
    }

    // ==== Comportamentos do domínio (passam por mutate) ====

    public void renew(LocalDate newEndDate, String actor) {
        mutate(actor, () -> {
            that(active, () -> "cannot renew inactive subscription");
            notNull(newEndDate, "newEndDate");
            that(endDate == null || newEndDate.isAfter(endDate),
                () -> "new endDate must be after current endDate");

            this.endDate = newEndDate;
            // Evento de renovação
            publishEvent(() -> new com.marcusprado02.sharedkernel.domain.model.example.events.SubscriptionRenewed(
                id(), newEndDate
            ));
        });
    }

    public void cancel(String reason, String actor) {
        mutate(actor, () -> {
            that(active, () -> "subscription already inactive");
            notNull(reason, "reason");

            this.active = false;
            // Evento de cancelamento
            publishEvent(() -> new com.marcusprado02.sharedkernel.domain.model.example.events.SubscriptionCancelled(
                id(), reason
            ));
        });
    }

    // ==== Regras/invariantes do agregado (obrigatória na nova AggregateRoot) ====
    @Override
    protected void validateAggregate() {
        that(planCode != null && !planCode.isBlank(), () -> "planCode must be present");
        that(startDate != null, () -> "startDate must be present");
        if (endDate != null) {
            that(endDate.isAfter(startDate), () -> "endDate must be after startDate");
        }
        // pode adicionar outras invariantes relacionadas a `active`, etc.
    }

    // ==== Getters controlados ====
    public String planCode() { return planCode; }
    public LocalDate startDate() { return startDate; }
    public LocalDate endDateOrNull() { return endDate; }
    public boolean isActive() { return active; }
}
