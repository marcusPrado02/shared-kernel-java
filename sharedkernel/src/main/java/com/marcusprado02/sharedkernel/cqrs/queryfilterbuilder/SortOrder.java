package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;

public record SortOrder(String field, boolean asc) {
    public static SortOrder asc(String f) { return new SortOrder(f, true); }
    public static SortOrder desc(String f){ return new SortOrder(f, false); }
}
