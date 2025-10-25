package com.marcusprado02.sharedkernel.contracts.graphql;

import java.util.List;

public record QuerySpec(
        List<Filter> filters,
        List<SortBy> sort,
        PageRequest page
) {
    public record Filter(String field, String op, List<String> values) {}
    public record SortBy(String field, String direction) {}
    public record PageRequest(Integer first, String after, Integer last, String before) {}
}

