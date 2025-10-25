package com.marcusprado02.sharedkernel.adapters.in.rest.pagination.jpa;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.CursorPayload;
import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.Order;

/** Contrato que "ensina" o adapter a montar JPQL e extrair keyset. */
public interface JpaQuerySpec<R> {
    Class<R> resultType();

    /** JPQL base (SELECT + WHERE) já com ORDER BY aplicado. */
    record JpqlPair(String query, String countQuery) {}

    JpqlPair toJpql(List<Order> sort);

    /** JPQL com predicado de keyset aplicado e ordenação possivelmente invertida. */
    JpqlPair toJpqlWithKeyset(List<Order> sort, String keysetPredicate, boolean reversed);

    /** Aplica parâmetros "fixos" (filtros) da consulta. */
    void applyParams(TypedQuery<?> tq);

    /** Aplica os valores do cursor (keyset) nos parâmetros do predicado. */
    void applyKeysetParams(TypedQuery<?> tq, CursorPayload key);

    /** Extrai (campo->valor) dos campos de ordenação a partir da linha retornada. */
    Map<String,Object> extractKeyValues(Object row, List<Order> sort);

    /** NOVO: predicado lexicográfico do keyset, ex.: "(f1,f2,id) > (:k1,:k2,:kid)" */
    String keysetPredicate(CursorPayload key, List<Order> sort);

    // Otimizações opcionais
    default boolean canCountCheap(){ return false; }
    default boolean canApproximateTotal(){ return false; }
    default Long totalApprox(EntityManager em){ return null; }
}
