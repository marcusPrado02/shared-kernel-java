package com.marcusprado02.sharedkernel.infrastructure.geo.adapter.mapbox;

import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodeOptions;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.GeocodingException;
import com.marcusprado02.sharedkernel.infrastructure.geo.api.Place;
import com.marcusprado02.sharedkernel.infrastructure.geo.spi.GeocodingProvider;

public class MapboxGeocodingProvider implements GeocodingProvider {
    private final OkHttpClient http;
    private final String apiKey; // MAPBOX_TOKEN
    private final MeterRegistry metrics;

    public MapboxGeocodingProvider(OkHttpClient http, String apiKey, MeterRegistry metrics) {
        this.http = http; this.apiKey = apiKey; this.metrics = metrics;
    }

    @Override public String name(){ return "mapbox"; }

    @Override
    public CompletableFuture<List<Place>> geocode(String query, GeocodeOptions o) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + encoded + ".json"
            + "?access_token=" + apiKey
            + "&limit=" + o.getLimit()
            + (o.getLocale()!=null? "&language="+o.getLocale().toLanguageTag() : "")
            + o.getProximity().map(p -> "&proximity="+p.lon()+","+p.lat()).orElse("")
            + (o.getCountryBias()!=null && !o.getCountryBias().isEmpty()? "&country="+String.join(",", o.getCountryBias()).toLowerCase() : "")
            + o.getBbox().map(b -> "&bbox=%s,%s,%s,%s".formatted(
                b.sw().lon(), b.sw().lat(), b.ne().lon(), b.ne().lat())).orElse("");

        Request req = new Request.Builder().url(url).get().build();
        Span span = Span.current().updateName("mapbox.geocode");
        span.addEvent("mapbox.request"); // evita warning de variável não usada

        CompletableFuture<List<Place>> cf = new CompletableFuture<>();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                metrics.counter("geocoding.provider.requests",
                        "provider", name(), "status", "io_failure").increment();
                cf.completeExceptionally(new GeocodingException(GeocodingException.Code.PROVIDER_UNAVAILABLE, name(),
                    "HTTP error", e));
            }
            @Override public void onResponse(Call call, Response resp) {
                try (resp) {
                    if (!resp.isSuccessful()) {
                        metrics.counter("geocoding.provider.requests",
                                "provider", name(), "status", String.valueOf(resp.code())).increment();
                    cf.completeExceptionally(new GeocodingException(mapStatus(resp.code()), name(),
                            "HTTP status "+resp.code(), null));
                        return;
                    }
                    String json = resp.body()!=null ? resp.body().string() : "";
                    List<Place> places = MapboxParser.parse(json, o.getMinConfidence());
                    metrics.counter("geocoding.provider.requests",
                            "provider", name(), "status", "ok").increment();
                    cf.complete(places);
                } catch (Exception ex) {
                    metrics.counter("geocoding.provider.requests",
                            "provider", name(), "status", "parse_error").increment();
                    cf.completeExceptionally(new GeocodingException(GeocodingException.Code.UNKNOWN, name(),
                        "Parsing error", ex));
                }
            }
        });
        return cf.orTimeout(o.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<List<Place>> reverse(double lat, double lon, GeocodeOptions o) {
        // Reverse usa {lon},{lat}.json
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + lon + "," + lat + ".json"
            + "?access_token=" + apiKey
            + "&limit=" + o.getLimit()
            + (o.getLocale()!=null? "&language="+o.getLocale().toLanguageTag() : "");

        Request req = new Request.Builder().url(url).get().build();
        Span span = Span.current().updateName("mapbox.reverse");
        span.addEvent("mapbox.request");

        CompletableFuture<List<Place>> cf = new CompletableFuture<>();

        http.newCall(req).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                metrics.counter("geocoding.provider.requests",
                        "provider", name(), "status", "io_failure").increment();
                cf.completeExceptionally(new GeocodingException(GeocodingException.Code.PROVIDER_UNAVAILABLE, name(),
                    "HTTP error", e));
            }
            @Override public void onResponse(Call call, Response resp) {
                try (resp) {
                    if (!resp.isSuccessful()) {
                        metrics.counter("geocoding.provider.requests",
                                "provider", name(), "status", String.valueOf(resp.code())).increment();
                        cf.completeExceptionally(new GeocodingException(mapStatus(resp.code()), name(),
                            "HTTP status "+resp.code(), null));
                        return;
                    }
                    String json = resp.body()!=null ? resp.body().string() : "";
                    List<Place> places = MapboxParser.parse(json, o.getMinConfidence());
                    metrics.counter("geocoding.provider.requests",
                            "provider", name(), "status", "ok").increment();
                    cf.complete(places);
                } catch (Exception ex) {
                    metrics.counter("geocoding.provider.requests",
                            "provider", name(), "status", "parse_error").increment();
                    cf.completeExceptionally(new GeocodingException(GeocodingException.Code.UNKNOWN, name(),
                        "Parsing error", ex));
                }
            }
        });
        return cf.orTimeout(o.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public ProviderCapabilities capabilities() {
        return new ProviderCapabilities(true, true, true);
    }

    private GeocodingException.Code mapStatus(int code) {
        return switch (code) {
            case 401, 403 -> GeocodingException.Code.UNAUTHORIZED;
            case 400, 422 -> GeocodingException.Code.BAD_REQUEST;
            case 429       -> GeocodingException.Code.RATE_LIMIT;
            case 408, 504  -> GeocodingException.Code.TIMEOUT;
            default        -> GeocodingException.Code.UNKNOWN;
        };
    }

    // -------- Parser interno (stub) --------
    // Mantém a assinatura apontada no seu código original.
    // Troque por uma implementação real (Jackson/org.json) quando quiser.
    private static final class MapboxParser {
        static List<Place> parse(String json, double minConfidence) {
            // TODO: implementar parsing real do JSON do Mapbox:
            // - features[].place_name
            // - features[].center -> [lon, lat]
            // - features[].relevance (usar minConfidence)
            // - features[].context (country/region/city)
            return List.of(); // por ora, vazio mas tipado (evita o erro de Object)
        }
    }
}
