package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.impl;

import org.springframework.data.jpa.domain.Specification;

import com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.*;

import jakarta.persistence.criteria.*;

import java.util.*;

public final class JpaSpecificationVisitor {

    public static <T> Specification<T> toSpecification(QueryFilter qf, FieldRegistry registry) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            for (var c : qf.criteria()) {
                var meta = registry.meta(c.field())
                        .orElseThrow(() -> new IllegalArgumentException("Campo não mapeado: " + c.field()));
                Path<?> path = resolvePath(root, meta.internalPath());

                Predicate p = switch (c.op()) {
                    case EQ -> cb.equal(path, c.values().get(0));
                    case NE -> cb.notEqual(path, c.values().get(0));
                    case GT -> cb.greaterThan((Expression<Comparable>) path, (Comparable) c.values().get(0));
                    case GE -> cb.greaterThanOrEqualTo((Expression<Comparable>) path, (Comparable) c.values().get(0));
                    case LT -> cb.lessThan((Expression<Comparable>) path, (Comparable) c.values().get(0));
                    case LE -> cb.lessThanOrEqualTo((Expression<Comparable>) path, (Comparable) c.values().get(0));
                    case IN -> path.in(c.values());
                    case NIN -> cb.not(path.in(c.values()));
                    case BETWEEN -> cb.between((Expression<Comparable>) path,
                            (Comparable) c.values().get(0), (Comparable) c.values().get(1));
                    case LIKE, ILIKE, CONTAINS, STARTS_WITH, ENDS_WITH -> likePredicate(cb, path, c);
                    case IS_NULL -> cb.isNull(path);
                    case NOT_NULL -> cb.isNotNull(path);
                    case EMPTY -> cb.or(cb.isNull(path), cb.equal(path, ""));
                    case NOT_EMPTY -> cb.and(cb.isNotNull(path), cb.notEqual(path, ""));
                    case EXISTS -> cb.isNotNull(path); // pode customizar para subquery
                };

                predicates.add(c.negated() ? cb.not(p) : p);
            }

            // Ordenação
            if (!qf.sort().isEmpty()) {
                var orders = new ArrayList<Order>();
                for (var s : qf.sort()) {
                    var meta = registry.meta(s.field()).orElseThrow();
                    var path = resolvePath(root, meta.internalPath());
                    orders.add(s.asc() ? cb.asc(path) : cb.desc(path));
                }
                query.orderBy(orders);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Path<?> resolvePath(From<?, ?> root, String dotPath) {
        Path<?> p = root;
        for (var seg : dotPath.split("\\.")) {
            // Join automático quando encontra associação (heurística simples)
            if (p instanceof From<?,?> from && isAssociation(from, seg)) {
                p = from.join(seg, JoinType.LEFT);
            } else {
                p = p.get(seg);
            }
        }
        return p;
    }

    private static boolean isAssociation(From<?,?> from, String attribute) {
        try { from.get(attribute).getJavaType(); return false; } catch (IllegalArgumentException e) { return true; }
    }

    private static Predicate likePredicate(CriteriaBuilder cb, Path<?> path, FilterCriterion c) {
        Expression<String> exp = path.as(String.class);
        String val = String.valueOf(c.values().get(0));
        String pat = switch (c.op()) {
            case STARTS_WITH -> val + "%";
            case ENDS_WITH -> "%" + val;
            case CONTAINS -> "%" + val + "%";
            case LIKE, ILIKE -> val;
            default -> throw new IllegalStateException();
        };
        if (c.op() == Operator.ILIKE) {
            return cb.like(cb.lower(exp), pat.toLowerCase());
        }
        return cb.like(exp, pat);
    }
}

