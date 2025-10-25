package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;


import java.util.*;

public final class QueryFilter {
    private final List<FilterCriterion> criteria;
    private final List<SortOrder> sort;
    private final Page page;
    private final Set<String> includes;   // projeções (opcional)
    private final Set<String> excludes;   // projeções (opcional)

    QueryFilter(List<FilterCriterion> criteria, List<SortOrder> sort, Page page,
                Set<String> includes, Set<String> excludes) {
        this.criteria = List.copyOf(criteria);
        this.sort = List.copyOf(sort);
        this.page = page;
        this.includes = includes == null ? Set.of() : Set.copyOf(includes);
        this.excludes = excludes == null ? Set.of() : Set.copyOf(excludes);
    }

    public List<FilterCriterion> criteria() { return criteria; }
    public List<SortOrder> sort() { return sort; }
    public Page page() { return page; }
    public Set<String> includes() { return includes; }
    public Set<String> excludes() { return excludes; }
}
