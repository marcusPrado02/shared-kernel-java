package com.marcusprado02.sharedkernel.infrastructure.search.adapter.sql;

import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.search.*;

public class SqlSearchAdapter<T> extends BaseSearchAdapter<T> {

    private final EntityManager em;
    private final Class<T> entityType;

    public SqlSearchAdapter(EntityManager em,
                            Class<T> entityType,
                            io.opentelemetry.api.trace.Tracer tracer,
                            io.micrometer.core.instrument.MeterRegistry meter,
                            io.github.resilience4j.retry.Retry retry,
                            io.github.resilience4j.circuitbreaker.CircuitBreaker cb,
                            CacheAdapter cache) {
        super(tracer, meter, retry, cb, cache);
        this.em = em;
        this.entityType = entityType;
    }

    @Override
    protected PageResult<T> doSearch(SearchQuery q, ProjectionMapper<Map<String, Object>, T> mapper) {
        var cbldr = em.getCriteriaBuilder();

        // Query principal
        CriteriaQuery<T> cq = cbldr.createQuery(entityType);
        Root<T> root = cq.from(entityType);

        // Filtros -> Predicate
        var predicates = new ArrayList<Predicate>();
        for (var c : q.filters()) {
            predicates.add(toPredicate(c, cbldr, root));
        }
        cq.where(predicates.toArray(Predicate[]::new));

        // Sort
        if (!q.sort().isEmpty()) {
            cq.orderBy(q.sort().stream()
                    .map(s -> s.direction() == Direction.ASC ? cbldr.asc(root.get(s.field())) : cbldr.desc(root.get(s.field())))
                    .collect(Collectors.toList()));
        }

        // Exec
        var typed = em.createQuery(cq);
        if (q.page().cursor() == null) {
            typed.setFirstResult(Math.max(q.page().page(), 0) * q.page().size());
            typed.setMaxResults(q.page().size());
        } // cursor: exigiria chave de ordenação estável e decode do token

        var list = typed.getResultList();

        // Count total
        CriteriaQuery<Long> countQ = cbldr.createQuery(Long.class);
        Root<T> countRoot = countQ.from(entityType);
        countQ.select(cbldr.count(countRoot)).where(predicates.toArray(Predicate[]::new));
        long total = em.createQuery(countQ).getSingleResult();

        // Mapeamento (para contrato comum, convertemos a entidade em Map<String,Object> antes de mapper)
        var hits = new ArrayList<T>(list);

        // Facets/Aggregations: executar queries extras por campo solicitado (ex.: COUNT GROUP BY)
        var facets = new HashMap<String, FacetResult>(); // placeholder
        var aggs = new HashMap<String, AggregationResult>(); // placeholder

        return new PageResult<>(
                hits, total, Math.max(q.page().page(), 0), q.page().size(),
                null, Duration.ZERO, facets, aggs, Map.of()
        );
    }

    private Predicate toPredicate(Criterion c, CriteriaBuilder cb, Root<T> root) {
        if (c instanceof Criterion.FieldCriterion fc) {
            Path<?> path = root.get(fc.path());

            switch (fc.op()) {
                case EQ:  return cb.equal(path, fc.value());
                case NE:  return cb.notEqual(path, fc.value());

                case GT: {
                    Comparable<?> v = (Comparable<?>) fc.value();
                    @SuppressWarnings("unchecked")
                    Class<? extends Comparable<?>> clazz = (Class<? extends Comparable<?>>) v.getClass();
                    @SuppressWarnings("unchecked")
                    Expression<? extends Comparable<?>> expr = (Expression<? extends Comparable<?>>) path.as(clazz);
                    return cb.greaterThan((Expression) expr, (Expression) cb.literal(v));
                }
                case GTE: {
                    Comparable<?> v = (Comparable<?>) fc.value();
                    @SuppressWarnings("unchecked")
                    Class<? extends Comparable<?>> clazz = (Class<? extends Comparable<?>>) v.getClass();
                    @SuppressWarnings("unchecked")
                    Expression<? extends Comparable<?>> expr = (Expression<? extends Comparable<?>>) path.as(clazz);
                    return cb.greaterThanOrEqualTo((Expression) expr, (Expression) cb.literal(v));
                }
                case LT: {
                    Comparable<?> v = (Comparable<?>) fc.value();
                    @SuppressWarnings("unchecked")
                    Class<? extends Comparable<?>> clazz = (Class<? extends Comparable<?>>) v.getClass();
                    @SuppressWarnings("unchecked")
                    Expression<? extends Comparable<?>> expr = (Expression<? extends Comparable<?>>) path.as(clazz);
                    return cb.lessThan((Expression) expr, (Expression) cb.literal(v));
                }
                case LTE: {
                    Comparable<?> v = (Comparable<?>) fc.value();
                    @SuppressWarnings("unchecked")
                    Class<? extends Comparable<?>> clazz = (Class<? extends Comparable<?>>) v.getClass();
                    @SuppressWarnings("unchecked")
                    Expression<? extends Comparable<?>> expr = (Expression<? extends Comparable<?>>) path.as(clazz);
                    return cb.lessThanOrEqualTo((Expression) expr, (Expression) cb.literal(v));
                }
                case BETWEEN: {
                    List<?> bounds = (List<?>) fc.value();
                    Comparable<?> lo = (Comparable<?>) bounds.get(0);
                    Comparable<?> hi = (Comparable<?>) bounds.get(1);
                    @SuppressWarnings("unchecked")
                    Class<? extends Comparable<?>> clazz =
                            (Class<? extends Comparable<?>>) lo.getClass(); // assumindo tipos compatíveis
                    @SuppressWarnings("unchecked")
                    Expression<? extends Comparable<?>> expr = (Expression<? extends Comparable<?>>) path.as(clazz);
                    // usa sempre Expressions nas 3 posições
                    return cb.between((Expression) expr, (Expression) cb.literal(lo), (Expression) cb.literal(hi));
                }

                case IN:
                    return path.in((Collection<?>) fc.value());

                case NIN:
                    return cb.not(path.in((Collection<?>) fc.value()));

                case LIKE:
                case MATCH:
                    return cb.like(path.as(String.class), "%" + fc.value() + "%");

                case EXISTS:
                    return cb.isNotNull(path);

                default:
                    return cb.conjunction();
            }
        } else if (c instanceof Criterion.BoolCriterion bc) {
            Predicate[] must    = bc.must().stream().map(cc -> toPredicate(cc, cb, root)).toArray(Predicate[]::new);
            Predicate[] should  = bc.should().stream().map(cc -> toPredicate(cc, cb, root)).toArray(Predicate[]::new);
            Predicate[] mustNot = bc.mustNot().stream().map(cc -> cb.not(toPredicate(cc, cb, root))).toArray(Predicate[]::new);

            Predicate p = cb.conjunction();
            if (must.length > 0)    p = cb.and(p, cb.and(must));
            if (should.length > 0)  p = cb.and(p, cb.or(should));
            if (mustNot.length > 0) p = cb.and(p, cb.and(mustNot));
            return p;
        }
        return cb.conjunction();
    }

}
