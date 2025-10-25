package com.marcusprado02.sharedkernel.infrastructure.geo.decorators;

import io.micrometer.core.instrument.*;
import io.opentelemetry.api.trace.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingClient;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;

public class TelemetryGeocodingClient implements GeocodingClient {
    private final GeocodingClient delegate;
    private final MeterRegistry metrics;
    private final Tracer tracer;

    public TelemetryGeocodingClient(GeocodingClient d, MeterRegistry m, Tracer t){ this.delegate=d; this.metrics=m; this.tracer=t;}

    @Override public CompletableFuture<List<Place>> geocode(String q, GeocodeOptions o) {
        var span = tracer.spanBuilder("geocoding.geocode").startSpan();
        long start = System.nanoTime();
        return delegate.geocode(q, o)
            .whenComplete((res, ex) -> {
                long durMs = (System.nanoTime()-start)/1_000_000;
                metrics.timer("geocoding.latency","op","geocode").record(durMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (ex==null) metrics.counter("geocoding.requests","op","geocode","status","ok").increment();
                else metrics.counter("geocoding.requests","op","geocode","status","error").increment();
                span.end();
            });
    }

    @Override public CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions o) {
        var span = tracer.spanBuilder("geocoding.reverse").startSpan();
        long start = System.nanoTime();
        return delegate.reverse(lat, lon, o)
            .whenComplete((res, ex) -> {
                long durMs = (System.nanoTime()-start)/1_000_000;
                metrics.timer("geocoding.latency","op","reverse").record(durMs, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (ex==null) metrics.counter("geocoding.requests","op","reverse","status","ok").increment();
                else metrics.counter("geocoding.requests","op","reverse","status","error").increment();
                span.end();
            });
    }
}
