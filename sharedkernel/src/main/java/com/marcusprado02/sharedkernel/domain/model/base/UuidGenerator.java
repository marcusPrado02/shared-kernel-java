package com.marcusprado02.sharedkernel.domain.model.base;

import java.util.UUID;

/** Gerador default (UUID v4). Pode trocar por ULID/UUIDv7 sem tocar o domínio. */
public final class UuidGenerator<ID extends Identifier> implements IdGenerator<ID> {
    private final java.util.function.Function<String, ID> factory;

    public UuidGenerator(java.util.function.Function<String, ID> factory) {
        this.factory = factory;
    }

    @Override public ID newId() { return factory.apply(UUID.randomUUID().toString()); }
}