package com.marcusprado02.sharedkernel.infrastructure.search;

public record Sort(String field, Direction direction, String mode /*min,max,avg*/ ) {
    public static Sort asc(String field)  { return new Sort(field, Direction.ASC, null); }
    public static Sort desc(String field) { return new Sort(field, Direction.DESC, null); }
}
