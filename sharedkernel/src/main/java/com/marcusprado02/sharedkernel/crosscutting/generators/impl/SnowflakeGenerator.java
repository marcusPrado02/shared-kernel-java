package com.marcusprado02.sharedkernel.crosscutting.generators.impl;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.marcusprado02.sharedkernel.crosscutting.generators.core.*;

public final class SnowflakeGenerator implements Generator<Long>, GeneratorProvider {
    private final long epochMs; private final long datacenterId; private final long workerId;
    private final AtomicLong lastTs = new AtomicLong(-1L);
    private long sequence = 0L;

    public SnowflakeGenerator(long epochMs, long datacenterId, long workerId){
        if (datacenterId<0||datacenterId>31 || workerId<0||workerId>31) throw new IllegalArgumentException();
        this.epochMs=epochMs; this.datacenterId=datacenterId; this.workerId=workerId;
    }

    @Override public synchronized Long generate(GenerationContext ctx) {
        long ts = Math.max(Instant.now(ctx.clock()).toEpochMilli(), lastTs.get());
        if (ts == lastTs.get()) { sequence = (sequence + 1) & 0xFFF; if (sequence==0) ts = waitNextMs(ts, ctx); }
        else sequence = 0L;
        lastTs.set(ts);
        return ((ts - epochMs) << 22) | (datacenterId << 17) | (workerId << 12) | sequence;
    }
    private static long waitNextMs(long ts, GenerationContext ctx){
        long n; do { n = Instant.now(ctx.clock()).toEpochMilli(); } while (n<=ts); return n;
    }

    // Provider
    @Override public boolean supports(java.net.URI uri){ return "gen".equals(uri.getScheme()) && "snowflake".equals(uri.getHost()); }
    @Override public Generator<?> create(java.net.URI uri, Map<String,?> defaults) {
        var q = uri.getQuery()==null?Map.<String,String>of():java.util.Arrays.stream(uri.getQuery().split("&"))
                .map(s->s.split("=")).collect(java.util.stream.Collectors.toMap(a->a[0], a->a[1]));
        long epoch = Long.parseLong(q.getOrDefault("epochMs", "1577836800000")); // 2020-01-01
        long dc = Long.parseLong(q.getOrDefault("dc", "0"));
        long worker = Long.parseLong(q.getOrDefault("worker", "0"));
        return new SnowflakeGenerator(epoch, dc, worker);
    }
}