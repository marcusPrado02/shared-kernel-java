package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.spec;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;

public interface SqlSpecification {
  Criteria toCriteria();
  static SqlSpecification of(Criteria c){ return () -> c; }
}
