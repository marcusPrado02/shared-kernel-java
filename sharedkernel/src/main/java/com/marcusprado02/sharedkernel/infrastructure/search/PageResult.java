package com.marcusprado02.sharedkernel.infrastructure.search;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

// -------- Resultados --------
public record PageResult<T>(
        List<T> hits,
        long total,
        int page,
        int size,
        String nextCursor,
        Duration took,
        Map<String, FacetResult> facets,
        Map<String, AggregationResult> aggregations,
        Map<String, Map<String, List<String>>> highlights // docId -> field -> fragments
) {
    public boolean hasNextPage() { return nextCursor != null && !nextCursor.isBlank(); }
}