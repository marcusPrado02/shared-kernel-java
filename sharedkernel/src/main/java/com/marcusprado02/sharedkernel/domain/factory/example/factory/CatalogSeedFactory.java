package com.marcusprado02.sharedkernel.domain.factory.example.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marcusprado02.sharedkernel.domain.factory.SeedFactory;
import com.marcusprado02.sharedkernel.domain.model.base.TenantId;
import com.marcusprado02.sharedkernel.domain.service.time.ClockProvider;

public final class CatalogSeedFactory implements SeedFactory {
    private static final Logger log = LoggerFactory.getLogger(CatalogSeedFactory.class);

    private final TenantId tenant;
    private final ClockProvider clock;

    public CatalogSeedFactory(TenantId tenant, ClockProvider clock) {
        this.tenant = tenant; this.clock = clock;
    }

    @Override
    public void seed() {
        // idempotente: antes de criar, checa existência
        // criar planos, SKUs, políticas padrão etc. via portas da camada de aplicação
        log.info("Seeding catalog for tenant={} at={}", tenant.asString(), clock.now());
        // ...
    }
}
