package com.marcusprado02.sharedkernel.cqrs.command.spi;

import java.time.Instant;

public interface ClockProvider {
    Instant nowUtc();
    static ClockProvider system(){ return () -> Instant.now(); }
}