package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model;

import java.time.Instant;
import java.util.Map;

public record Point(
  String measurement,                 // ex: cpu, tx, booking_event
  Map<String,String> tags,            // ex: host, region, tenantId
  Map<String,FieldValue> fields,      // ex: usage -> 0.82, status -> "OK"
  Instant timestamp                   // precisão: nanos preferível
) { public String tag(String k){ return tags.get(k); } }
