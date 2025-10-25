package com.marcusprado02.sharedkernel.observability.logging.structured;


import java.nio.file.Path;
import java.util.*;

import com.marcusprado02.sharedkernel.observability.logging.LogContext;
import com.marcusprado02.sharedkernel.observability.logging.LogEnricher;
import com.marcusprado02.sharedkernel.observability.logging.structured.sink.ConsoleSink;
import com.marcusprado02.sharedkernel.observability.logging.structured.sink.FileSink;

public final class StructuredLoggerFactory {
    public static final class Builder {
        String name = "app";
        LogEnricher enricher;
        Encoder<byte[]> encoder = new JsonEncoder();
        List<LogSink> sinks = new ArrayList<>();
        int capacity = 8192;
        AsyncStructuredLogger.DropPolicy dropPolicy = AsyncStructuredLogger.DropPolicy.DROP_OLDEST;
        AsyncStructuredLogger.RateLimiter limiter = AsyncStructuredLogger.tokenBucket(500.0, 200);
        LogContext ctx;
        Map<String,Object> fixed = Map.of();

        public Builder name(String n){ this.name = n; return this; }
        public Builder enricher(LogEnricher e){ this.enricher = e; return this; }
        public Builder encoder(Encoder<byte[]> enc){ this.encoder = enc; return this; }
        public Builder sink(LogSink s){ this.sinks.add(s); return this; }
        public Builder capacity(int c){ this.capacity = c; return this; }
        public Builder dropPolicy(AsyncStructuredLogger.DropPolicy p){ this.dropPolicy = p; return this; }
        public Builder rateLimiter(AsyncStructuredLogger.RateLimiter r){ this.limiter = r; return this; }
        public Builder context(LogContext c){ this.ctx = c; return this; }
        public Builder fixedFields(Map<String,Object> f){ this.fixed = f; return this; }

        public StructuredLogger build() {
            Objects.requireNonNull(enricher, "enricher é obrigatório");
            if (sinks.isEmpty()) sinks.add(new ConsoleSink());
            return new AsyncStructuredLogger(name, enricher, encoder, sinks, capacity, dropPolicy, limiter, ctx, fixed);
        }
    }
    private StructuredLoggerFactory(){}

    public static Builder builder(){ return new Builder(); }

    /** Atalhos */
    public static StructuredLogger consoleDefault(String name, LogEnricher e, LogContext c) {
        return builder().name(name).enricher(e).context(c).sink(new ConsoleSink()).build();
    }
    public static StructuredLogger fileDefault(String name, LogEnricher e, LogContext c, Path file) throws Exception {
        return builder().name(name).enricher(e).context(c).sink(new FileSink(file)).build();
    }
}
