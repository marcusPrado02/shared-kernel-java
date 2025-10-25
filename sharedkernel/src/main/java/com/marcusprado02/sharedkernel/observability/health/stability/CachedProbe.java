package com.marcusprado02.sharedkernel.observability.health.stability;

import java.time.*;

import com.marcusprado02.sharedkernel.observability.health.ProbeCheck;
import com.marcusprado02.sharedkernel.observability.health.ProbeResult;

public final class CachedProbe implements ProbeCheck {
    private final ProbeCheck delegate;
    private final Duration ttl;
    private volatile ProbeResult last;
    private volatile Instant expiresAt = Instant.EPOCH;

    public CachedProbe(ProbeCheck delegate, Duration ttl) { this.delegate = delegate; this.ttl = ttl; }

    @Override public ProbeResult check() {
        var now = Instant.now();
        if (now.isBefore(expiresAt) && last != null) return last;
        var start = Instant.now();
        var res = delegate.check();
        last = new ProbeResult(res.status(), res.reason(), res.details(), Duration.between(start, Instant.now()));
        expiresAt = now.plus(ttl);
        return last;
    }
    @Override public String name() { return delegate.name(); }
}