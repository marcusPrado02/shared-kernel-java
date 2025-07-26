package com.marcusprado02.sharedkernel.domain.policyhandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import com.marcusprado02.sharedkernel.domain.policy.TypedBusinessPolicy;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PolicyHandler implements BusinessPolicyHandler {


    private final Map<Class<?>, TypedBusinessPolicy<?>> policyMap;

    /**
     * Constrói o mapa de Class→Policy automaticamente a partir dos beans TypedBusinessPolicy<T>
     * disponíveis.
     */
    public PolicyHandler(List<TypedBusinessPolicy<?>> policies) {
        this.policyMap = policies.stream()
                .collect(Collectors.toMap(TypedBusinessPolicy::getTargetType, p -> p));
    }

    /**
     * Aplica a política para a instância passada.
     *
     * @throws IllegalStateException se não existir policy para o tipo.
     */
    @SuppressWarnings("unchecked")
    public <T> void handle(T candidate) {
        TypedBusinessPolicy<T> policy =
                (TypedBusinessPolicy<T>) policyMap.get(candidate.getClass());

        if (policy == null) {
            throw new IllegalStateException(
                    "Nenhuma policy registrada para " + candidate.getClass().getSimpleName());
        }
        policy.enforce(candidate);
    }
}

