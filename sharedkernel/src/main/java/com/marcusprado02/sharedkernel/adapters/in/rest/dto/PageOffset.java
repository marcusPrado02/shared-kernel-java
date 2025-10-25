package com.marcusprado02.sharedkernel.adapters.in.rest.dto;

/** Paginação por offset/limit. */
public record PageOffset(
        int offset,
        int limit,
        long total
) {}
