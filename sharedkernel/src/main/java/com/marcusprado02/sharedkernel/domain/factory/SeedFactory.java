package com.marcusprado02.sharedkernel.domain.factory;

/** Inicializa dados de catálogo/parametrizações para ambientes novos. */
public interface SeedFactory {
    void seed(); // idempotente
}
