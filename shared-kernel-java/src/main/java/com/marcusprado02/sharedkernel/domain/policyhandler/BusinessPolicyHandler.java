package com.marcusprado02.sharedkernel.domain.policyhandler;

import com.marcusprado02.sharedkernel.domain.policy.TypedBusinessPolicy;

/**
 * Componente responsável por localizar e aplicar {@link TypedBusinessPolicy}s para um determinado
 * objeto de domínio.
 *
 * A implementação típica (ex.: {@code PolicyHandler}) resolve a policy pelo tipo concreto do
 * candidato e executa {@link TypedBusinessPolicy#enforce(Object)}.
 */
public interface BusinessPolicyHandler {

    /**
     * Aplica a(s) regra(s) de negócio correspondentes ao tipo do candidato.
     *
     * @param candidate objeto a ser validado.
     * @param <T> tipo do objeto.
     * @throws IllegalStateException se não houver policy registrada para o tipo.
     */
    <T> void handle(T candidate);
}
