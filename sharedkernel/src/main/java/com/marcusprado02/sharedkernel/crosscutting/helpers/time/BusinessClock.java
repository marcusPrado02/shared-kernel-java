package com.marcusprado02.sharedkernel.crosscutting.helpers.time;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class BusinessClock {
  private final Clock clock;
  public BusinessClock(Clock clock){ this.clock = clock; }
  public Instant now(){ return clock.instant(); }
  public ZonedDateTime nowAt(ZoneId zone){ return ZonedDateTime.now(clock.withZone(zone)); }
}

