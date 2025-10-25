package com.marcusprado02.sharedkernel.observability.chaos;

public interface ChaosCondition {
    /** Retorna probabilidade ∈ [0..1] dada a observação do contexto; 0 = nunca, 1 = sempre. */
    double probability(ChaosContext ctx);
    /** Texto explicativo (útil para debug/auditoria). */
    default String describe(){ return getClass().getSimpleName(); }
}