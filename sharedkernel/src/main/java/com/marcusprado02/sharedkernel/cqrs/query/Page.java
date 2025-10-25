package com.marcusprado02.sharedkernel.cqrs.query;

import java.util.List;
import java.util.Optional;

/** Página baseada em offset/limit OU cursor (mutuamente exclusivos). */
public record Page<T>(
        List<T> items,
        int size,
        long total,          // opcional (−1 quando desconhecido para cursor)
        Optional<String> nextCursor,
        Optional<String> prevCursor
) {
    public static <T> Page<T> of(List<T> items, int size, long total){ return new Page<>(List.copyOf(items), size, total, Optional.empty(), Optional.empty()); }
    public static <T> Page<T> withCursor(List<T> items, String next){ return new Page<>(List.copyOf(items), items.size(), -1, Optional.ofNullable(next), Optional.empty()); }
}
