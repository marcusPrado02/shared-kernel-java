package com.marcusprado02.sharedkernel.domain.factory;

public interface SnapshotFactory<A, S> {
    /** Produz um snapshot estável do agregado A para S (DTO/VO), sem “vazar” infra. */
    S snapshotOf(A aggregate);
}
