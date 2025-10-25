package com.marcusprado02.sharedkernel.application.projector;

public interface Projector<T> extends Projection {
    /** Aplica um evento/mensagem de domínio à visão. Deve ser idempotente. */
    void apply(T event) throws Exception;
}
