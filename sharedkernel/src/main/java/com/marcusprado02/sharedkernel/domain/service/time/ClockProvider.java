package com.marcusprado02.sharedkernel.domain.service.time;


import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public interface ClockProvider {
    Clock clock();
    default Instant now() { return Instant.now(clock()); }
    static ClockProvider systemUTC() {
        return () -> Clock.systemUTC();
    }
    static ClockProvider fixed(Instant instant, ZoneId zone) {
        return () -> Clock.fixed(instant, zone);
    }
}
