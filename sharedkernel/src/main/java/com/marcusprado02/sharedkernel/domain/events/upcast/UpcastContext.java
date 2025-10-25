package com.marcusprado02.sharedkernel.domain.events.upcast;

import java.util.Map;

public record UpcastContext(Map<String, Object> attributes) {
    public static UpcastContext empty() { return new UpcastContext(Map.of()); }
}
