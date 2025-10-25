package com.marcusprado02.sharedkernel.domain.id.api;

public interface ClockSource {
    /** Milliseconds since Unix epoch (UTC). */
    long currentTimeMillis();
}