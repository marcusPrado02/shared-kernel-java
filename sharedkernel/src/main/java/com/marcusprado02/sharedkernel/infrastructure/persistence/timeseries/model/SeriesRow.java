package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record SeriesRow(
  Instant ts,                         // bucket boundary ou ts do sample
  Map<String,String> tags,            // grouping tags retornadas
  Map<String,Object> values           // campos/agregações retornados
) {
  @Override public String toString() {
    return "SeriesRow{ts=%s, tags=%s, values=%s}".formatted(ts, tags, values);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SeriesRow that)) return false;
    return ts.equals(that.ts) && tags.equals(that.tags) && values.equals(that.values);
  }

  @Override public int hashCode() {
    return Objects.hash(ts, tags, values);
  }
  
}
