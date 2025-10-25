package com.marcusprado02.sharedkernel.infrastructure.search.adapter.memory;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.marcusprado02.sharedkernel.infrastructure.search.BaseSearchAdapter;
import com.marcusprado02.sharedkernel.infrastructure.search.CacheAdapter;
import com.marcusprado02.sharedkernel.infrastructure.search.Criterion;
import com.marcusprado02.sharedkernel.infrastructure.search.Direction;
import com.marcusprado02.sharedkernel.infrastructure.search.FieldMask;
import com.marcusprado02.sharedkernel.infrastructure.search.PageResult;
import com.marcusprado02.sharedkernel.infrastructure.search.ProjectionMapper;
import com.marcusprado02.sharedkernel.infrastructure.search.SearchQuery;

public class InMemorySearchAdapter<T> extends BaseSearchAdapter<T> {

    private final List<Map<String, Object>> store = Collections.synchronizedList(new ArrayList<>());
    private final Function<Map<String,Object>, T> materializer;

    public InMemorySearchAdapter(Function<Map<String,Object>, T> materializer,
                                 io.opentelemetry.api.trace.Tracer tracer,
                                 io.micrometer.core.instrument.MeterRegistry meter,
                                 io.github.resilience4j.retry.Retry retry,
                                 io.github.resilience4j.circuitbreaker.CircuitBreaker cb,
                                 CacheAdapter cache) {
        super(tracer, meter, retry, cb, cache);
        this.materializer = materializer;
    }

    public void add(Map<String, Object> doc) { store.add(doc); }

    @Override
    protected PageResult<T> doSearch(SearchQuery q, ProjectionMapper<Map<String, Object>, T> mapper) {
        List<Map<String, Object>> filtered = store.stream()
                .filter(doc -> matchesAll(q.filters(), doc))
                .collect(Collectors.toList());

        // Sort (apenas String/Number/Comparable simples)
        for (var s : q.sort()) {
            Comparator<Map<String, Object>> cmp = Comparator.comparing(m -> (Comparable) m.get(s.field()), Comparator.nullsLast(Comparator.naturalOrder()));
            if (s.direction() == Direction.DESC) cmp = cmp.reversed();
            filtered.sort(cmp);
        }

        int from = q.page().cursor() == null ? Math.max(q.page().page(), 0) * q.page().size() : 0;
        int to = Math.min(from + q.page().size(), filtered.size());
        var window = from >= filtered.size() ? List.<Map<String, Object>>of() : filtered.subList(from, to);

        var hits = window.stream()
                .map(doc -> mapper.map(doc, q.fieldMask().orElse(new FieldMask(Set.of(), Set.of()))))
                .collect(Collectors.toList());

        return new PageResult<>(hits, filtered.size(),
                Math.max(q.page().page(), 0), q.page().size(),
                null, Duration.ZERO, Map.of(), Map.of(), Map.of());
    }

    private boolean matchesAll(List<Criterion> criteria, Map<String,Object> doc) {
        for (var c : criteria) {
            if (!matches(c, doc)) return false;
        }
        return true;
    }

    private boolean matches(Criterion c, Map<String,Object> doc) {
        if (c instanceof Criterion.FieldCriterion fc) {
            Object v = doc.get(fc.path());
            return switch (fc.op()) {
                case EQ -> Objects.equals(v, fc.value());
                case NE -> !Objects.equals(v, fc.value());
                case GT -> ((Comparable) v).compareTo(fc.value()) > 0;
                case GTE -> ((Comparable) v).compareTo(fc.value()) >= 0;
                case LT -> ((Comparable) v).compareTo(fc.value()) < 0;
                case LTE -> ((Comparable) v).compareTo(fc.value()) <= 0;
                case IN -> ((Collection<?>) fc.value()).contains(v);
                case NIN -> !((Collection<?>) fc.value()).contains(v);
                case BETWEEN -> {
                    var b = (List<?>) fc.value();
                    yield ((Comparable) v).compareTo(b.get(0)) >= 0 && ((Comparable) v).compareTo(b.get(1)) <= 0;
                }
                case LIKE, MATCH -> v != null && v.toString().toLowerCase().contains(fc.value().toString().toLowerCase());
                case EXISTS -> v != null;
                default -> true;
            };
        } else if (c instanceof Criterion.BoolCriterion bc) {
            boolean must = bc.must().stream().allMatch(cc -> matches(cc, doc));
            boolean should = bc.should().isEmpty() || bc.should().stream().anyMatch(cc -> matches(cc, doc));
            boolean not = bc.mustNot().stream().noneMatch(cc -> matches(cc, doc));
            return must && should && not;
        }
        return true;
    }
}

