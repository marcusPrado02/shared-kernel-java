package com.marcusprado02.sharedkernel.crosscutting.interceptors.adapter.feign;


import feign.RequestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.crosscutting.interceptors.core.Carrier;

/** Carrier para RequestTemplate (Feign). */
public final class FeignCarrier implements Carrier {
    private final RequestTemplate template;

    public FeignCarrier(RequestTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<String> get(String key) {
        Collection<String> values = template.headers().get(key);
        if (values == null || values.isEmpty()) return Optional.empty();
        // Convencão: retorna o primeiro valor
        return Optional.of(values.iterator().next());
    }

    @Override
    public void set(String key, String value) {
        // Limpa valores anteriores e define um único valor
        template.header(key);           // clear
        template.header(key, value);    // set
    }

    @Override
    public Map<String, String> dump() {
        // Junta múltiplos valores por vírgula (convenção comum em HTTP)
        return template.headers().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> String.join(",", e.getValue())
            ));
    }
}

