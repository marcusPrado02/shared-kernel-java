package com.marcusprado02.sharedkernel.saga;

import java.util.Optional;

public interface SagaStore {
    <D extends SagaData> void save(SagaInstance<D> s);
    <D extends SagaData> Optional<SagaInstance<D>> find(String sagaId, Class<D> type);
    boolean tryUpdateVersion(String sagaId, int expected, java.util.function.Consumer<SagaInstance<?>> mutator);
}
