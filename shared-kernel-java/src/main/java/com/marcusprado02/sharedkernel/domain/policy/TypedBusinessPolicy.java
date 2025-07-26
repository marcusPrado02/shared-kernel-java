package com.marcusprado02.sharedkernel.domain.policy;

/**
 * Extensão de BusinessPolicy que declara explicitamente qual tipo T ela atende.
 */
public interface TypedBusinessPolicy<T> extends BusinessPolicy<T> {
    /** Informa a classe de domínio que esta policy trata */
    Class<T> getTargetType();
}
