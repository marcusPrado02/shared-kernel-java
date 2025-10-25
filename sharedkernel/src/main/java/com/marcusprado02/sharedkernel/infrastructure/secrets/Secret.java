package com.marcusprado02.sharedkernel.infrastructure.secrets;

import java.time.Instant;
import java.util.Map;

public record Secret<T>(
        SecretId id,
        SecretType type,
        T value,                    // String/Map<String,Object>/byte[]/PEM
        String versionId,
        String stage,
        Instant createdAt,
        Instant expiresAt,
        Map<String, String> metadata,
        String etag                 // p/ condicional/otimismo
) {}
