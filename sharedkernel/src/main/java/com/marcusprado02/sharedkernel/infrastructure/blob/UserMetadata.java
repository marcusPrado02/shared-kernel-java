package com.marcusprado02.sharedkernel.infrastructure.blob;

import java.util.Map;

public record UserMetadata(Map<String, String> values) {
    public static UserMetadata of(Map<String, String> m){ return new UserMetadata(Map.copyOf(m)); }
}
