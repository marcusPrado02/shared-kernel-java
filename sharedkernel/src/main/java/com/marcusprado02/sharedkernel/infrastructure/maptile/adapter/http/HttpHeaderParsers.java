package com.marcusprado02.sharedkernel.infrastructure.maptile.adapter.http;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class HttpHeaderParsers {
    private HttpHeaderParsers(){}

    public static int cacheTtlSeconds(Optional<String> cacheControl, Optional<String> expires) {
        if (cacheControl.isPresent()) {
            String cc = cacheControl.get();
            for (String token : cc.split(",")) {
                token = token.trim().toLowerCase();
                if (token.startsWith("max-age=")) {
                    try { return Integer.parseInt(token.substring(8)); } catch (NumberFormatException ignored) {}
                }
            }
        }
        if (expires.isPresent()) {
            try {
                Instant exp = ZonedDateTime.parse(expires.get(), DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
                long secs = exp.getEpochSecond() - Instant.now().getEpochSecond();
                return (int) Math.max(secs, 0);
            } catch (Exception ignored) {}
        }
        // fallback: 10min
        return 600;
    }

    public static Instant lastModified(Optional<String> lm) {
        return lm.map(v -> {
            try { return ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(); }
            catch (Exception e) { return null; }
        }).orElse(null);
    }
}
