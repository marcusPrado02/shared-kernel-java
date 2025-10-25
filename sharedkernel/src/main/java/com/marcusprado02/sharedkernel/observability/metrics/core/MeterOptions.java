package com.marcusprado02.sharedkernel.observability.metrics.core;


import java.time.Duration;
import java.util.*;

public final class MeterOptions {
    private final boolean percentileHistogram;
    private final double[] percentiles;         // ex: 0.5, 0.9, 0.99
    private final double[] slaBoundaries;       // ex: millis buckets para SLAs
    private final Duration expiry;              // janela de agregação
    private final Map<String, String> extraTags;

    private MeterOptions(Builder b) {
        this.percentileHistogram = b.percentileHistogram;
        this.percentiles = b.percentiles.clone();
        this.slaBoundaries = b.slaBoundaries.clone();
        this.expiry = b.expiry;
        this.extraTags = Collections.unmodifiableMap(new LinkedHashMap<>(b.extraTags));
    }

    public static Builder builder() { return new Builder(); }

    public boolean percentileHistogram() { return percentileHistogram; }
    public double[] percentiles() { return percentiles.clone(); }
    public double[] slaBoundaries() { return slaBoundaries.clone(); }
    public Duration expiry() { return expiry; }
    public Map<String, String> extraTags() { return extraTags; }

    public static final class Builder {
        private boolean percentileHistogram;
        private double[] percentiles = new double[0];
        private double[] slaBoundaries = new double[0];
        private Duration expiry = Duration.ofMinutes(2);
        private final Map<String,String> extraTags = new LinkedHashMap<>();

        public Builder percentileHistogram(boolean v){ this.percentileHistogram = v; return this; }
        public Builder percentiles(double... ps){ this.percentiles = ps!=null? ps : new double[0]; return this; }
        public Builder slaBoundaries(double... bs){ this.slaBoundaries = bs!=null? bs : new double[0]; return this; }
        public Builder expiry(Duration d){ this.expiry = d; return this; }
        public Builder tag(String k, String v){ if(k!=null && v!=null) this.extraTags.put(k,v); return this; }
        public Builder tags(Map<String,String> ts){ if(ts!=null) this.extraTags.putAll(ts); return this; }
        public MeterOptions build(){ return new MeterOptions(this); }
    }
}