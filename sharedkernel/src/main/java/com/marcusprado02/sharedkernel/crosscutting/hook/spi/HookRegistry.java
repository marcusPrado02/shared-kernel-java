package com.marcusprado02.sharedkernel.crosscutting.hook.spi;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.crosscutting.hook.*;

public final class HookRegistry {
    private final List<HookProvider> providers;
    public HookRegistry(List<HookProvider> providers){ this.providers=List.copyOf(providers); }
    public static HookRegistry loadDefault(){
        var sl = java.util.ServiceLoader.load(HookProvider.class);
        return new HookRegistry(sl.stream().map(java.util.ServiceLoader.Provider::get).toList());
    }
    public HookRegistration<?> resolve(String uri, Telemetry t){
        var u = URI.create(uri);
        return providers.stream().filter(p -> p.supports(u)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No provider: "+uri))
                .create(u, Map.of(), t);
    }
}
