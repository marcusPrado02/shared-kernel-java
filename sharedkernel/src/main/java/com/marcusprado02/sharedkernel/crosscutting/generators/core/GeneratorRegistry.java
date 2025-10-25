package com.marcusprado02.sharedkernel.crosscutting.generators.core;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class GeneratorRegistry {
    private final List<GeneratorProvider> providers;
    private final Map<String,Object> defaults;
    public GeneratorRegistry(List<GeneratorProvider> providers, Map<String,?> defaults){
        this.providers = List.copyOf(providers);
        this.defaults = Map.copyOf(defaults);
    }
    public static GeneratorRegistry loadDefault() {
        ServiceLoader<GeneratorProvider> sl = ServiceLoader.load(GeneratorProvider.class);
        return new GeneratorRegistry(sl.stream().map(ServiceLoader.Provider::get).toList(), Map.of());
    }
    @SuppressWarnings("unchecked")
    public <T> Generator<T> resolve(String uri){
        URI u = URI.create(uri);
        for (GeneratorProvider p : providers) if (p.supports(u)) {
            return (Generator<T>) p.create(u, defaults);
        }
        throw new IllegalArgumentException("No provider for " + uri);
    }
}