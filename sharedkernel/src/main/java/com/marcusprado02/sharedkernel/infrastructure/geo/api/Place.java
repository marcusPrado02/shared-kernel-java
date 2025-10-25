package com.marcusprado02.sharedkernel.infrastructure.geo.api;

import java.util.Map;

public record Place(
    String id,
    String formattedAddress,
    Address address,
    Geometry geometry,
    double confidence,              // 0..1 normalizado
    String source,                  // "google","mapbox","here","nominatim"
    Map<String, Object> sourceMeta  // campos específicos do provedor
) {}
