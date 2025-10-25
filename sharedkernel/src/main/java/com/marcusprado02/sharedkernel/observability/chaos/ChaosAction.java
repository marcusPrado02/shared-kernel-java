package com.marcusprado02.sharedkernel.observability.chaos;

public interface ChaosAction {
    /** Executa a ação (pode bloquear, lançar exceção, etc.). Deve ser **idempotente** por chamada. */
    void apply(ChaosContext ctx) throws Exception;

    /** Nome curto e estável (ex.: latency, exception, drop). */
    String name();
}