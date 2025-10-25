package com.marcusprado02.sharedkernel.infrastructure.geo.api;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;

public final class GeocodeOptions {
    public enum Strategy { PREFERRED, FALLBACK, HEDGED, QUORUM }
    private final Locale locale;
    private final Set<String> countryBias;       // ex: ["BR","US"]
    private final Optional<BoundingBox> bbox;    // delimitar área
    private final Optional<Point> proximity;     // "near"
    private final int limit;
    private final double minConfidence;          // 0..1
    private final Strategy strategy;
    private final Optional<String> preferredProvider;
    private final Duration timeout;
    private final Function<String, List<Place>> batchCollector;

    // Builder imutável
    // ... getters
    public static Builder builder() { return new Builder(); }
    public static final class Builder {
        private Locale locale = Locale.forLanguageTag("pt-BR");
        private Set<String> countryBias = Set.of("BR");
        private Optional<BoundingBox> bbox = Optional.empty();
        private Optional<Point> proximity = Optional.empty();
        private int limit = 5;
        private double minConfidence = 0.0;
        private Strategy strategy = Strategy.PREFERRED;
        private Optional<String> preferredProvider = Optional.empty();
        private Duration timeout = Duration.ofSeconds(3);
        private Function<String, List<Place>> batchCollector = __ -> List.of();
        // ... setters fluentes
        public GeocodeOptions build(){ return new GeocodeOptions(this); }
    }
    private GeocodeOptions(Builder b){ 
        this.locale = b.locale;
        this.countryBias = Set.copyOf(b.countryBias);
        this.bbox = b.bbox;
        this.proximity = b.proximity;
        this.limit = Math.max(1, Math.min(20, b.limit));
        this.minConfidence = Math.max(0.0, Math.min(1.0, b.minConfidence));
        this.strategy = b.strategy;
        this.preferredProvider = b.preferredProvider;
        this.timeout = b.timeout;
        this.batchCollector = b.batchCollector;
     }

    public Locale getLocale() { return locale; }
    public Set<String> getCountryBias() { return countryBias; }
    public Optional<BoundingBox> getBbox() { return bbox; }
    public Optional<Point> getProximity() { return proximity; }
    public int getLimit() { return limit; }
    public double getMinConfidence() { return minConfidence; }
    public Strategy getStrategy() { return strategy; }
    public Optional<String> getPreferredProvider() { return preferredProvider; }
    public Duration getTimeout() { return timeout; }
    public Function<String, List<Place>> getBatchCollector() { return batchCollector; }
}