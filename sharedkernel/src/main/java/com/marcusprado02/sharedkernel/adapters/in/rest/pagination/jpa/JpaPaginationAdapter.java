package com.marcusprado02.sharedkernel.adapters.in.rest.pagination.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.*;

import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.*;

public final class JpaPaginationAdapter<R> implements PaginationAdapter<JpaQuerySpec<R>, R> {
    private final EntityManager em;
    private final CursorCodec codec;

    public JpaPaginationAdapter(EntityManager em, CursorCodec codec) { this.em = em; this.codec = codec; }

    @Override public PageResult<R> page(JpaQuerySpec<R> spec, PageRequestSpec pr) {
        return switch (pr.mode()) {
            case OFFSET -> pageOffset(spec, pr);
            case CURSOR -> pageKeyset(spec, pr);
        };
    }

    private PageResult<R> pageOffset(JpaQuerySpec<R> spec, PageRequestSpec pr){
        var jpql = spec.toJpql(pr.sort());         // "select e from Entity e where ... order by ..."
        var tq = em.createQuery(jpql.query(), spec.resultType());
        spec.applyParams(tq);
        tq.setFirstResult(pr.offset());
        tq.setMaxResults(pr.limit()+1);
        var rows = tq.getResultList();
        boolean hasMore = rows.size() > pr.limit();
        var items = hasMore ? rows.subList(0, pr.limit()) : rows;
        Long total = null;
        if (spec.canCountCheap()) {
            var c = em.createQuery(jpql.countQuery(), Long.class);
            spec.applyParams(c);
            total = c.getSingleResult();
        }
        String next = null;
        if (hasMore) {
            var last = items.get(items.size()-1);
            var cursorKv = spec.extractKeyValues(last, pr.sort()); // campo->valor do último
            next = codec.encode(codec.makeFromRow(cursorKv, Direction.ASC));
        }
        return new PageResult<>(items, hasMore, total, null, next, null);
    }

    private PageResult<R> pageKeyset(JpaQuerySpec<R> spec, PageRequestSpec pr){
        var afterPayload = codec.decode(pr.after());
        var beforePayload = codec.decode(pr.before());

        var direction = resolveDirection(afterPayload, beforePayload, pr.sort());
        var keysetPredicate = spec.keysetPredicate(direction.payload(), pr.sort()); // e.g. (f1,f2,id) > (:v1,:v2,:id)
        var jpql = spec.toJpqlWithKeyset(pr.sort(), keysetPredicate, direction.reverse()); // inverte order se for "before"
        var tq = em.createQuery(jpql.query(), spec.resultType());
        spec.applyParams(tq);
        spec.applyKeysetParams(tq, direction.payload());

        tq.setMaxResults(pr.limit()+1);
        var raw = tq.getResultList();

        // se fizemos reverse para "before", precisamos reordenar de volta para ASC natural
        var items = direction.reverse() ? reverseAndTrim(raw, pr.limit()) : trim(raw, pr.limit());
        boolean hasMore = raw.size() > pr.limit();

        String next = null; String prev = null;
        if (!items.isEmpty()) {
            var last = items.get(items.size()-1);
            var first = items.get(0);
            next = hasMore ? codec.encode(codec.makeFromRow(spec.extractKeyValues(last, pr.sort()), Direction.ASC)) : null;
            prev = codec.encode(codec.makeFromRow(spec.extractKeyValues(first, pr.sort()), Direction.DESC));
        }
        Long totalApprox = spec.canApproximateTotal() ? spec.totalApprox(em) : null;
        return new PageResult<>(items, hasMore, null, totalApprox, next, prev);
    }

    private static <T> List<T> trim(List<T> rows, int limit){
        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }
    private static <T> List<T> reverseAndTrim(List<T> rows, int limit){
        Collections.reverse(rows);
        return trim(rows, limit);
    }

    private record Dir(CursorPayload payload, boolean reverse) {}
    private Dir resolveDirection(CursorPayload after, CursorPayload before, List<Order> sort){
        if (before != null) return new Dir(before, true);   // buscar anteriores → consulta invertida
        if (after  != null) return new Dir(after, false);   // buscar próximos → consulta normal
        return new Dir(new CursorPayload(Map.of(), Direction.ASC, java.time.Instant.now()), false);
    }
}
