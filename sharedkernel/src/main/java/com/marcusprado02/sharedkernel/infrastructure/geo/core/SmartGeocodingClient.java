package com.marcusprado02.sharedkernel.infrastructure.geo.core;


import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;
import com.marcusprado02.sharedkernel.infrastructure.geo.spi.GeocodingProvider;

public class SmartGeocodingClient implements GeocodingClient {
    private final List<GeocodingProvider> providers;
    private final Executor hedgedPool;
    private final int quorum; // para Strategy.QUORUM

    public SmartGeocodingClient(List<GeocodingProvider> providers, Executor hedgedPool, int quorum) {
        this.providers = List.copyOf(providers);
        this.hedgedPool = hedgedPool;
        this.quorum = quorum;
    }

    @Override public CompletableFuture<List<Place>> geocode(String query, GeocodeOptions o) {
        return switch (o.getStrategy()) {
            case PREFERRED -> callPreferred(query, o);
            case FALLBACK  -> callFallback(query, o);
            case HEDGED    -> callHedged(query, o);
            case QUORUM    -> callQuorum(query, o);
        };
    }

    @Override public CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions o) {
        return switch (o.getStrategy()) {
            case PREFERRED -> reversePreferred(lat, lon, o);
            case FALLBACK  -> reverseFallback(lat, lon, o);
            case HEDGED    -> reverseHedged(lat, lon, o);
            case QUORUM    -> reverseQuorum(lat, lon, o);
        };
    }

    private CompletableFuture<List<Place>> callPreferred(String q, GeocodeOptions o) {
        var p = pickPreferred(o);
        return invokeGeocode(p, q, o).thenApply(list -> filterAndLimit(list, o));
    }

    private CompletableFuture<List<Place>> callFallback(String q, GeocodeOptions o) {
        CompletableFuture<List<Place>> cf = new CompletableFuture<>();
        callFallbackChain(0, q, o, cf);
        return cf;
    }
    private void callFallbackChain(int idx, String q, GeocodeOptions o, CompletableFuture<List<Place>> cf) {
        if (idx >= providers.size()) { cf.complete(List.of()); return; }
        invokeGeocode(providers.get(idx), q, o).thenApply(list -> filterAndLimit(list, o))
            .whenComplete((res, ex) -> {
                if (ex == null && !res.isEmpty()) cf.complete(res);
                else callFallbackChain(idx+1, q, o, cf);
            });
    }

    private CompletableFuture<List<Place>> callHedged(String q, GeocodeOptions o) {
        List<CompletableFuture<List<Place>>> calls = providers.stream()
            .map(p -> CompletableFuture.supplyAsync(() -> p, hedgedPool)
                .thenCompose(pp -> invokeGeocode(pp, q, o))
                .thenApply(list -> filterAndLimit(list, o)))
            .toList();
        return firstSuccessful(calls);
    }

    private CompletableFuture<List<Place>> callQuorum(String q, GeocodeOptions o) {
        List<CompletableFuture<List<Place>>> calls = providers.stream()
            .map(p -> invokeGeocode(p, q, o).thenApply(list -> filterAndLimit(list, o)))
            .toList();
        return CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new))
            .thenApply(v -> calls.stream().map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator.comparingDouble(Place::confidence).reversed())
                .limit(o.getLimit())
                .toList());
    }

    private CompletableFuture<List<Place>> reversePreferred(double lat, double lon, GeocodeOptions o) {
        var p = pickPreferred(o);
        return invokeReverse(p, lat, lon, o).thenApply(list -> filterAndLimit(list, o));
    }
    private CompletableFuture<List<Place>> reverseFallback(double lat, double lon, GeocodeOptions o) {
        CompletableFuture<List<Place>> cf = new CompletableFuture<>();
        reverseFallbackChain(0, lat, lon, o, cf); return cf;
    }
    private void reverseFallbackChain(int idx, double lat, double lon, GeocodeOptions o, CompletableFuture<List<Place>> cf) {
        if (idx >= providers.size()) { cf.complete(List.of()); return; }
        invokeReverse(providers.get(idx), lat, lon, o).thenApply(list -> filterAndLimit(list, o))
            .whenComplete((res, ex) -> {
                if (ex == null && !res.isEmpty()) cf.complete(res);
                else reverseFallbackChain(idx+1, lat, lon, o, cf);
            });
    }
    private CompletableFuture<List<Place>> reverseHedged(double lat, double lon, GeocodeOptions o) {
        List<CompletableFuture<List<Place>>> calls = providers.stream()
            .map(p -> invokeReverse(p, lat, lon, o).thenApply(list -> filterAndLimit(list, o))).toList();
        return firstSuccessful(calls);
    }
    private CompletableFuture<List<Place>> reverseQuorum(double lat, double lon, GeocodeOptions o) {
        List<CompletableFuture<List<Place>>> calls = providers.stream()
            .map(p -> invokeReverse(p, lat, lon, o).thenApply(list -> filterAndLimit(list, o))).toList();
        return CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new))
            .thenApply(v -> calls.stream().map(CompletableFuture::join)
                .flatMap(List::stream)
                .sorted(Comparator.comparingDouble(Place::confidence).reversed())
                .limit(o.getLimit()).toList());
    }

    private GeocodingProvider pickPreferred(GeocodeOptions o) {
        return o.getPreferredProvider()
            .flatMap(n -> providers.stream().filter(p -> p.name().equalsIgnoreCase(n)).findFirst())
            .orElse(providers.get(0));
    }

    private static CompletableFuture<List<Place>> firstSuccessful(List<CompletableFuture<List<Place>>> calls) {
        CompletableFuture<List<Place>> cf = new CompletableFuture<>();
        calls.forEach(c -> c.whenComplete((res, ex) -> {
            if (!cf.isDone() && ex == null && !res.isEmpty()) cf.complete(res);
        }));
        CompletableFuture.allOf(calls.toArray(CompletableFuture[]::new)).whenComplete((v, ex) -> {
            if (!cf.isDone()) cf.complete(List.of());
        });
        return cf;
    }

    private CompletableFuture<List<Place>> invokeGeocode(GeocodingProvider p, String q, GeocodeOptions o) {
        try {
            var m = p.getClass().getMethod("geocode", String.class, GeocodeOptions.class);
            Object res = m.invoke(p, q, o);
            @SuppressWarnings("unchecked")
            CompletableFuture<List<Place>> cf = (CompletableFuture<List<Place>>) res;
            return cf;
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private CompletableFuture<List<Place>> invokeReverse(GeocodingProvider p, double lat, double lon, GeocodeOptions o) {
        try {
            var m = p.getClass().getMethod("reverse", double.class, double.class, GeocodeOptions.class);
            Object res = m.invoke(p, lat, lon, o);
            @SuppressWarnings("unchecked")
            CompletableFuture<List<Place>> cf = (CompletableFuture<List<Place>>) res;
            return cf;
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private List<Place> filterAndLimit(List<Place> list, GeocodeOptions o) {
        return list.stream()
            .filter(p -> p.confidence() >= o.getMinConfidence())
            .limit(o.getLimit())
            .collect(Collectors.toList());
    }
}

