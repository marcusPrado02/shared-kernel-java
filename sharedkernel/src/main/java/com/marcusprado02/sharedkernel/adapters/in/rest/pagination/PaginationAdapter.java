package com.marcusprado02.sharedkernel.adapters.in.rest.pagination;

/** Adaptador unificado. */
public interface PaginationAdapter<Q, R> {
    /**
     * Executa consulta paginada conforme espec., retornando PageResult de R (row ou entidade).
     * Q = tipo de "query" do backend (JPA Criteria/Specification, SQL DSL, Filter Doc Mongo).
     */
    PageResult<R> page(Q query, PageRequestSpec spec);
}