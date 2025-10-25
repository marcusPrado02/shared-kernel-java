package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;

import java.time.Instant;
import java.util.Map;

/** Cursor canônico: pares (campo -> valor), direção de varredura e timestamp. */
public record CursorPayload(
        Map<String, Object> keyValues,
        Direction direction,
        Instant issuedAt
) {}
