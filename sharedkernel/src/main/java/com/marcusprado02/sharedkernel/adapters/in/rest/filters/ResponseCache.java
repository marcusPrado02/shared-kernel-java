package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import java.util.Optional;

import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;

public interface ResponseCache {
    Optional<ApiResponse> get(String key);
    void put(String key, ApiResponse resp, long ttlMillis);
}
