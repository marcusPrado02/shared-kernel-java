package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query;

import java.time.Duration;

public record Bucket(Duration every, Duration offset, String timezone) {
  public static Bucket of(Duration every){ return new Bucket(every, Duration.ZERO, "UTC"); }
}
