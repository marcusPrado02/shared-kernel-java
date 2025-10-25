package com.marcusprado02.sharedkernel.infrastructure.persistence.criteria;

import java.util.*;
public record Criteria(List<Filter> filters, Optional<Sort> sort) {
  public static Criteria of(Filter... fs){ return new Criteria(List.of(fs), Optional.empty()); }
  public Criteria and(Filter f){ var l=new ArrayList<>(filters); l.add(f); return new Criteria(l, sort); }
  public Criteria sortBy(Sort s){ return new Criteria(filters, Optional.ofNullable(s)); }
}