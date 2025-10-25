package com.marcusprado02.sharedkernel.infrastructure.geo.api;


import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface GeocodingClient {
    CompletableFuture<List<Place>> geocode(String query, GeocodeOptions options);
    CompletableFuture<List<Place>> reverse(double latitude, double longitude, GeocodeOptions options);
    default CompletableFuture<List<Place>> batchGeocode(List<String> queries, GeocodeOptions options) {
        List<CompletableFuture<List<Place>>> futures = queries.stream()
            .map(q -> geocode(q, options))
            .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .toList());
    }
}
