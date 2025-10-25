package com.marcusprado02.sharedkernel.infrastructure.persistence.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort; // Sort do Spring

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;

/** Utilitários para converter nosso Criteria em Specification JPA. */
public final class Specs {

  private Specs() {}

  /** Converte nosso Criteria em Specification<E> tipada. */
  public static <E> Specification<E> toJpaSpecification(Criteria c) {
    return (root, query, cb) -> {
      List<Predicate> ps = new ArrayList<>();
      for (Filter f : c.filters()) {
        Path<?> path = root.get(f.field());
        switch (f.op()) {
          case EQ  -> ps.add(cb.equal(path, f.value()));
          case NE  -> ps.add(cb.notEqual(path, f.value()));
          case GT  -> ps.add(cb.greaterThan(path.as(Comparable.class), (Comparable) f.value()));
          case GTE -> ps.add(cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) f.value()));
          case LT  -> ps.add(cb.lessThan(path.as(Comparable.class), (Comparable) f.value()));
          case LTE -> ps.add(cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) f.value()));
          case LIKE -> {
            // case-insensitive LIKE simples
            ps.add(cb.like(cb.lower(path.as(String.class)), String.valueOf(f.value()).toLowerCase()));
          }
          case IN -> ps.add(path.in((Collection<?>) f.value()));
          case BETWEEN -> {
            var vals = (List<?>) f.value();
            ps.add(cb.between(path.as(Comparable.class), (Comparable) vals.get(0), (Comparable) vals.get(1)));
          }
          case IS_NULL  -> ps.add(cb.isNull(path));
          case NOT_NULL -> ps.add(cb.isNotNull(path));
        }
      }
      return ps.isEmpty() ? cb.conjunction() : cb.and(ps.toArray(new Predicate[0]));
    };
  }

  /** Converte nosso Sort opcional para Sort do Spring. */
  public static Sort toSpringSort(Optional<com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Sort> s) {
    return s.map(ss -> ss.asc()
        ? Sort.by(ss.field()).ascending()
        : Sort.by(ss.field()).descending())
      .orElse(Sort.unsorted());
  }
}
