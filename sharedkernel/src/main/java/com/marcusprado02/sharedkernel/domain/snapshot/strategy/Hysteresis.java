package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

public record Hysteresis(long minEventGap, long minMillisGap) {
    public static Hysteresis none() { return new Hysteresis(0, 0); }
}
