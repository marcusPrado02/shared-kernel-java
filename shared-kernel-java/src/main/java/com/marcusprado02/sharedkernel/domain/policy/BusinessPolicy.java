package com.marcusprado02.sharedkernel.domain.policy;

/**
 * Políticas aplicáveis a um objeto de domínio.
 */
public interface BusinessPolicy<T> {
    /**
     * Executa todas as regras da política, lançando exceção em caso de qualquer violação.
     */
    void enforce(T candidate);
}
