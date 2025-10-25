package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// -------- Contrato principal --------
public interface SearchAdapter<T> {
    PageResult<T> search(SearchQuery query, ProjectionMapper<Map<String, Object>, T> mapper);

    CompletableFuture<PageResult<T>> searchAsync(SearchQuery query, ProjectionMapper<Map<String, Object>, T> mapper);

    default String backendName() { return getClass().getSimpleName(); }
}
