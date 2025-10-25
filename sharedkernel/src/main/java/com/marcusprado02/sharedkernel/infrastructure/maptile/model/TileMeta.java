package com.marcusprado02.sharedkernel.infrastructure.maptile.model;

public record TileMeta(
        String providerId, EtagInfo etag, long ttlSeconds, long fetchedAtMillis
) {}