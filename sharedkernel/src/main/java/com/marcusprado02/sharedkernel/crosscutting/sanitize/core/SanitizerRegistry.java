package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;


import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public final class SanitizerRegistry {
    private final List<SanitizerProvider> providers;
    private final Map<String,?> defaults;

    public SanitizerRegistry(List<SanitizerProvider> providers, Map<String,?> defaults){
        this.providers = List.copyOf(providers); this.defaults = Map.copyOf(defaults);
    }
    public static SanitizerRegistry loadDefault(){
        ServiceLoader<SanitizerProvider> sl = ServiceLoader.load(SanitizerProvider.class);
        return new SanitizerRegistry(sl.stream().map(ServiceLoader.Provider::get).toList(), Map.of());
    }
    @SuppressWarnings("unchecked")
    public <T> Sanitizer<T> resolve(String uri){
        URI u = URI.create(uri);
        for (SanitizerProvider p : providers) if (p.supports(u)) return (Sanitizer<T>) p.create(u, defaults);
        throw new IllegalArgumentException("No provider for " + uri);
    }
}