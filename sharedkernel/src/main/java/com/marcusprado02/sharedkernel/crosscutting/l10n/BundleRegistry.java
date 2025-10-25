package com.marcusprado02.sharedkernel.crosscutting.l10n;

import java.net.URI;
import java.util.*;

import com.marcusprado02.sharedkernel.crosscutting.l10n.spi.BundleStoreProvider;

public final class BundleRegistry {
    private final List<BundleStoreProvider> providers;
    public BundleRegistry(List<BundleStoreProvider> ps){ this.providers=List.copyOf(ps); }
    public static BundleRegistry loadDefault(){
        var sl = ServiceLoader.load(BundleStoreProvider.class);
        return new BundleRegistry(sl.stream().map(ServiceLoader.Provider::get).toList());
    }
    public BundleStore resolve(String uri, Map<String,?> defaults){
        var u = URI.create(uri);
        return providers.stream().filter(p -> p.supports(u)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No provider for "+uri))
                .create(u, defaults);
    }
}

