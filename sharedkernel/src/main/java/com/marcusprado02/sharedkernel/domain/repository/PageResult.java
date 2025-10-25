package com.marcusprado02.sharedkernel.domain.repository;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, long totalElements, int page, int size) {
    public <U> PageResult<U> map(Function<T,U> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), totalElements, page, size);
    }
}
