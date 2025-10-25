package com.marcusprado02.sharedkernel.crosscutting.l10n.reload;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.marcusprado02.sharedkernel.crosscutting.l10n.BundleStore;

public final class ReloadableBundleStore implements BundleStore {
    private final BundleStore delegate;
    private final ConcurrentHashMap<String, Map<String,String>> shadow = new ConcurrentHashMap<>();
    public ReloadableBundleStore(BundleStore delegate){ this.delegate=delegate; }
    public void invalidateAll(){ shadow.clear(); } // chamado pelo watcher
    @Override public Optional<String> getMessage(String tenant, Locale locale, String key) {
        String k = "%s|%s".formatted(tenant, locale.toString());
        Map<String,String> map = shadow.computeIfAbsent(k, _k -> {
            // copia materializada de keys->values para rápido acesso
            Map<String,String> m = new HashMap<>();
            for (String kk : delegate.keys(tenant, locale)) {
                delegate.getMessage(tenant, locale, kk).ifPresent(v -> m.put(kk, v));
            }
            return Map.copyOf(m);
        });
        return Optional.ofNullable(map.get(key));
    }
    @Override public Set<String> keys(String tenant, Locale locale){ return delegate.keys(tenant, locale); }
}
