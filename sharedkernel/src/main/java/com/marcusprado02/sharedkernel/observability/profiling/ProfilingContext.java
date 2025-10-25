package com.marcusprado02.sharedkernel.observability.profiling;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ProfilingContext {
    // Snapshot imutável do estado observado
    public final Instant now;
    public final double systemCpuLoad;           // 0.0..1.0
    public final double processCpuLoad;          // 0.0..1.0
    public final long   usedHeapBytes;
    public final long   maxHeapBytes;
    public final double allocationRateBytesPerSec;
    public final double errorRatePerMin;         // erros/min
    public final double p95LatencyMs;
    public final double p99LatencyMs;
    public final long   youngGcCount1m;
    public final long   fullGcCount5m;
    public final double loadAverage1m;
    public final long   queueDepth;              // ex.: fila assíncrona
    public final Map<String, Double> customGauges; // extensões

    private ProfilingContext(Builder b){ /* assign...*/ 
        this.now = b.now;
        this.systemCpuLoad = b.systemCpuLoad; this.processCpuLoad = b.processCpuLoad;
        this.usedHeapBytes = b.usedHeapBytes; this.maxHeapBytes = b.maxHeapBytes;
        this.allocationRateBytesPerSec = b.allocationRateBytesPerSec;
        this.errorRatePerMin = b.errorRatePerMin; this.p95LatencyMs = b.p95LatencyMs; this.p99LatencyMs = b.p99LatencyMs;
        this.youngGcCount1m = b.youngGcCount1m; this.fullGcCount5m = b.fullGcCount5m;
        this.loadAverage1m = b.loadAverage1m; this.queueDepth = b.queueDepth;
        this.customGauges = Collections.unmodifiableMap(new HashMap<>(b.customGauges));
    }
    public static class Builder {
        private Instant now = Instant.now();
        private double systemCpuLoad, processCpuLoad, allocationRateBytesPerSec, errorRatePerMin, p95LatencyMs, p99LatencyMs, loadAverage1m;
        private long usedHeapBytes, maxHeapBytes, youngGcCount1m, fullGcCount5m, queueDepth;
        private Map<String, Double> customGauges = new HashMap<>();
        // setters encadeados...
        public Builder now(Instant v){ this.now=v; return this; }
        public Builder systemCpu(double v){ this.systemCpuLoad=v; return this; }
        public Builder processCpu(double v){ this.processCpuLoad=v; return this; }
        public Builder usedHeap(long v){ this.usedHeapBytes=v; return this; }
        public Builder maxHeap(long v){ this.maxHeapBytes=v; return this; }
        public Builder allocRate(double v){ this.allocationRateBytesPerSec=v; return this; }
        public Builder errRate(double v){ this.errorRatePerMin=v; return this; }
        public Builder p95(double v){ this.p95LatencyMs=v; return this; }
        public Builder p99(double v){ this.p99LatencyMs=v; return this; }
        public Builder youngGc1m(long v){ this.youngGcCount1m=v; return this; }
        public Builder fullGc5m(long v){ this.fullGcCount5m=v; return this; }
        public Builder load1m(double v){ this.loadAverage1m=v; return this; }
        public Builder queueDepth(long v){ this.queueDepth=v; return this; }
        public Builder gauge(String k, double v){ this.customGauges.put(k, v); return this; }
        public ProfilingContext build(){ return new ProfilingContext(this); }
    }
}