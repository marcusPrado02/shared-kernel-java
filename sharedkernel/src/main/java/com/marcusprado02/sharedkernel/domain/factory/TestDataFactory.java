package com.marcusprado02.sharedkernel.domain.factory;

import java.util.function.Supplier;

/** Testes/fixtures determinísticos (clock/seed controlados). */
public interface TestDataFactory<T> extends Supplier<T> {
    @Override T get();
}
