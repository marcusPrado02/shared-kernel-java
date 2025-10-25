package com.marcusprado02.sharedkernel.adapters.in.rest.filters;

import java.util.Optional;

import com.marcusprado02.sharedkernel.contracts.api.ApiResponse;

public interface IdempotencyStore {
    Optional<ApiResponse> find(String key);
    void save(String key, ApiResponse response);
}
