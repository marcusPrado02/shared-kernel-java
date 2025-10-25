package com.marcusprado02.sharedkernel.cqrs.timemachine;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public final class VirtualClock implements TimeProvider {
    private final AtomicReference<Instant> base = new AtomicReference<>(Instant.EPOCH);
    private final AtomicReference<Double> speed = new AtomicReference<>(1.0); // 1.0 = tempo real
    private final AtomicLong t0 = new AtomicLong(System.nanoTime());
    public static VirtualClock frozenAt(Instant t) { var c = new VirtualClock(); c.freezeAt(t); return c; }
    @Override public Instant now() {
        var elapsedNs = (long)((System.nanoTime() - t0.get()) * speed.get());
        return base.get().plusNanos(elapsedNs);
    }
    public void freezeAt(Instant t){ base.set(t); t0.set(System.nanoTime()); speed.set(0.0); }
    public void setSpeed(double factor){ base.set(now()); t0.set(System.nanoTime()); speed.set(factor); }
    public void advance(Duration d){ base.set(now().plus(d)); t0.set(System.nanoTime()); }
}
