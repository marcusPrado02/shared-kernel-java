package com.marcusprado02.sharedkernel.contracts.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PageResponse<T>(
        java.util.List<T> data,
        long totalElements,
        int totalPages,
        int page,
        int size,
        Map<String, String> links) {}
