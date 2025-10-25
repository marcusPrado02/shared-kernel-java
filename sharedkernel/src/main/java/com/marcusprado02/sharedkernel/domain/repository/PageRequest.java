package com.marcusprado02.sharedkernel.domain.repository;

import java.util.List;

public record PageRequest(int page, int size, List<Sort> sorts) {
    public static PageRequest of(int page, int size, Sort... sorts) {
        return new PageRequest(page, size, List.of(sorts));
    }
}
