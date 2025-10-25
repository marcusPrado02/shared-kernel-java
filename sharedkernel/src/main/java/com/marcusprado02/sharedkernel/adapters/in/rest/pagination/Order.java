package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;

/** Campo de ordenação com estratégia de nulls. */
public record Order(
        String field,
        Direction direction,
        Nulls nulls // FIRST/LAST/DB_DEFAULT
) {
    public enum Nulls { FIRST, LAST, DB_DEFAULT }
}
