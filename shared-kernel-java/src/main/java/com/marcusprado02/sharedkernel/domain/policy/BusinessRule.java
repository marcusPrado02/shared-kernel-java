package com.marcusprado02.sharedkernel.domain.policy;

/**
 * Regra de negócio genérica: testa uma entidade e lança exceção se não for satisfeita.
 */
public interface BusinessRule<T> {
    /**
     * Retorna true se a regra for satisfeita pelo objeto.
     */
    boolean isSatisfiedBy(T candidate);

    /**
     * Mensagem de erro caso a regra não seja satisfeita.
     */
    String message();

    /**
     * Valida a regra ou lança BusinessRuleViolationException.
     */
    default void check(T candidate) {
        if (!isSatisfiedBy(candidate)) {
            // TODO: Exceção personalizada
        }
    }
}
