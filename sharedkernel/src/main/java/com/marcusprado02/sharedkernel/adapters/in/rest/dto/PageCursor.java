package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Paginação por cursor – estável para alto volume. */
public record PageCursor(
        String before,
        String after,
        Integer limit,
        Boolean hasMore,
        Long totalApprox
) {}
