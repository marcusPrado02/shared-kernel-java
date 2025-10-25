package com.marcusprado02.sharedkernel.domain.id.example.impl;

import com.marcusprado02.sharedkernel.domain.id.api.ClockSource;

public final class SystemClockSource implements ClockSource {
    @Override public long currentTimeMillis() { return System.currentTimeMillis(); }
}
