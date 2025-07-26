package com.marcusprado02.sharedkernel.domain.policy;


import java.util.List;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

/**
 * Política genérica que recebe um conjunto de Rules e aplica todas numa entidade T.
 *
 * Obs.: para cada tipo T você pode ter um @Qualifier no constructor ou definir uma Collection de
 * beans mais específica.
 */
@Component
@RequiredArgsConstructor
public class GenericPolicy<T> implements BusinessPolicy<T> {

    private final List<BusinessRule<T>> rules;

    @Override
    public void enforce(T candidate) {
        for (BusinessRule<T> rule : rules) {
            rule.check(candidate);
        }
    }
}
