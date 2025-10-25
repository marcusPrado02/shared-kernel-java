package com.marcusprado02.sharedkernel.infrastructure.geo.decorators;

import com.github.benmanes.caffeine.cache.*;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

public class CachedGeocodingClient implements GeocodingClient {
    private final GeocodingClient delegate;
    private final Cache<String, List<Place>> cache;

    public CachedGeocodingClient(GeocodingClient delegate, Cache<String, List<Place>> cache) {
        this.delegate = delegate; this.cache = cache;
    }

    @Override public CompletableFuture<List<Place>> geocode(String q, GeocodeOptions o) {
        String key = key("fwd", q, o);
        var cached = cache.getIfPresent(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);
        return delegate.geocode(q, o).thenApply(list -> {
            cache.put(key, list);
            return list;
        });
    }
    @Override public CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions o) {
        String key = key("rev", lat+","+lon, o);
        var cached = cache.getIfPresent(key);
        if (cached != null) return CompletableFuture.completedFuture(cached);
        return delegate.reverse(lat, lon, o).thenApply(list -> { cache.put(key, list); return list;});
    }

    private String key(String kind, String q, GeocodeOptions o) {
        String raw = kind+"|"+q+"|"+o.getLocale()+"|"+o.getCountryBias()+"|"+o.getLimit()+"|"+o.getMinConfidence()+"|"+
            o.getBbox().map(Object::toString).orElse("")+"|"+o.getProximity().map(Object::toString).orElse("");
        return sha256(raw);
    }
    private static String sha256(String s){
        try{
            var md = MessageDigest.getInstance("SHA-256");
            var b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            var sb = new StringBuilder(); for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        }catch(Exception e){ throw new RuntimeException(e); }
    }
}