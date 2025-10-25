package com.marcusprado02.sharedkernel.domain.snapshot.ports;

public interface SizeEstimator<S> {
    /** Estima bytes do estado em memória/serializado (heurística). */
    long estimateBytes(S state);
}