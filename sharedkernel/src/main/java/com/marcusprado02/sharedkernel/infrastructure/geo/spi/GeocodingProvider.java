package com.marcusprado02.sharedkernel.infrastructure.geo.spi;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;

public interface GeocodingProvider {
    String name(); // ex: "google"
    CompletableFuture<List<Place>> geocode(String query, GeocodeOptions options);
    CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions options);
    ProviderCapabilities capabilities();

    record ProviderCapabilities(boolean supportsBatch, boolean supportsBias, boolean supportsProximity) {}
}