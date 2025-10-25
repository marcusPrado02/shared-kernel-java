package com.marcusprado02.sharedkernel.observability.logging.structured;


import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.marcusprado02.sharedkernel.observability.logging.LogContext;
import com.marcusprado02.sharedkernel.observability.logging.LogEnricher;
import com.marcusprado02.sharedkernel.observability.logging.LogEvent;
import com.marcusprado02.sharedkernel.observability.logging.Severity;

public final class AsyncStructuredLogger implements StructuredLogger, AutoCloseable {
    public enum DropPolicy { BLOCK, DROP_OLDEST, DROP_NEW }
    public interface RateLimiter { boolean allow(Severity level, String loggerName); }

    private final String name;
    private final LogEnricher enricher;             // seu StandardLogEnricher
    private final Encoder<byte[]> encoder;          // JsonEncoder
    private final List<LogSink> sinks;              // Console/File/Http…
    private final BlockingQueue<StructuredRecord> queue;
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final DropPolicy dropPolicy;
    private final RateLimiter rateLimiter;
    private final LogContext ctx;

    private final Map<String,Object> fixed;         // campos fixos (scoped logger)

    public AsyncStructuredLogger(String name,
                                 LogEnricher enricher,
                                 Encoder<byte[]> encoder,
                                 List<LogSink> sinks,
                                 int queueCapacity,
                                 DropPolicy dropPolicy,
                                 RateLimiter rateLimiter,
                                 LogContext ctx,
                                 Map<String,Object> fixedFields) {
        this.name = name;
        this.enricher = enricher;
        this.encoder = encoder;
        this.sinks = List.copyOf(sinks);
        this.queue = new ArrayBlockingQueue<>(Math.max(queueCapacity, 128));
        this.dropPolicy = dropPolicy;
        this.rateLimiter = rateLimiter;
        this.ctx = ctx==null? new LogContext(name, "unknown","unknown","unknown", Map.of()) : ctx;
        this.fixed = fixedFields==null? Map.of() : Map.copyOf(fixedFields);

        this.worker = new Thread(this::drainLoop, "structured-logger-"+name);
        this.worker.setDaemon(true);
        this.worker.start();
    }

    @Override public void log(Severity level, String message, Map<String,Object> fields, Throwable error) {
        if (rateLimiter != null && !rateLimiter.allow(level, name)) return;
        var now = Instant.now();
        var rec = new StructuredRecord(now, level, name, Thread.currentThread().getName(), message, error, merge(fields));
        offer(rec);
    }

    private Map<String,Object> merge(Map<String,Object> fields) {
        if (fixed.isEmpty()) return fields==null? Map.of() : fields;
        Map<String,Object> out = new LinkedHashMap<>(fixed);
        if (fields!=null) out.putAll(fields);
        return out;
    }

    private void offer(StructuredRecord rec){
        boolean ok = queue.offer(rec);
        if (ok) return;
        switch (dropPolicy) {
            case BLOCK -> {
                try { queue.put(rec); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
            case DROP_OLDEST -> { queue.poll(); queue.offer(rec); }
            case DROP_NEW -> { /* drop */ }
        }
    }

    private void drainLoop() {
        while (running.get() || !queue.isEmpty()) {
            try {
                StructuredRecord rec = queue.poll(250, TimeUnit.MILLISECONDS);
                if (rec == null) continue;

                // Enriquecer (PII-safe, correlação, etc.)
                var event = new LogEvent(rec.ts,
                        map(rec.level), rec.logger, rec.thread, rec.message, rec.error, rec.fields);
                Map<String,Object> enriched = enricher.enrich(event, ctx);

                // Re-encapsular para encoder (mantendo schema simples)
                var finalRec = new StructuredRecord(rec.ts, rec.level, rec.logger, rec.thread,
                        (String) enriched.getOrDefault("message", rec.message),
                        rec.error, enriched);

                byte[] bytes = encoder.encode(finalRec);
                for (var s : sinks) try { s.write(bytes); } catch (Exception ignore) {}
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Throwable t) {
                // Nunca derrubar o worker
            }
        }
        // flush
        for (var s : sinks) try { s.flush(); } catch (Exception ignore) {}
    }

    private Severity map(Severity s) {
        return switch (s) {
            case TRACE -> Severity.TRACE;
            case DEBUG -> Severity.DEBUG;
            case INFO  -> Severity.INFO;
            case WARN  -> Severity.WARN;
            default    -> Severity.ERROR;
        };
    }

    @Override public StructuredLogger withFields(Map<String,Object> fixedFields) {
        Map<String,Object> merged = new LinkedHashMap<>(this.fixed);
        if (fixedFields!=null) merged.putAll(fixedFields);
        return new AsyncStructuredLogger(this.name, this.enricher, this.encoder, this.sinks,
                this.queue.remainingCapacity() + this.queue.size(), this.dropPolicy, this.rateLimiter, this.ctx, merged);
    }

    @Override public String name(){ return name; }

    @Override public void close() {
        running.set(false);
        try { worker.join(1500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        for (var s : sinks) try { s.close(); } catch (Exception ignore) {}
    }

    /* --------- RateLimiter helper --------- */
    public static RateLimiter tokenBucket(double perSecond, int burst) {
        final double refillPerNs = perSecond / 1_000_000_000d;
        final int max = Math.max(1, burst);
        final Map<String, double[]> buckets = new ConcurrentHashMap<>();
        return (level, loggerName) -> {
            String key = level.ordinal() >= Severity.WARN.ordinal() ? "hi-"+loggerName : "lo-"+loggerName;
            double[] state = buckets.computeIfAbsent(key, k -> new double[]{max, System.nanoTime()});
            synchronized (state) {
                long now = System.nanoTime();
                double tokens = state[0] + (now - state[1]) * refillPerNs;
                state[0] = Math.min(max, tokens);
                state[1] = now;
                if (state[0] >= 1.0) { state[0] -= 1.0; return true; }
                return level.ordinal() >= Severity.WARN.ordinal(); // WARN+ sempre passa
            }
        };
    }
}

