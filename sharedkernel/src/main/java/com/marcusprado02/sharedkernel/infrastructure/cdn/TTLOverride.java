package com.marcusprado02.sharedkernel.infrastructure.cdn;

import java.time.Duration;

public record TTLOverride(Duration staleWhileRevalidate, Duration staleIfError) {
    public static TTLOverride none() { return new TTLOverride(null, null); }
}