package com.marcusprado02.sharedkernel.domain.specification;

/**
 * Adapters (JPA/Mongo/Elastic) podem reconhecer QuerySpecifications
 * e convertê-los para Criteria/DSL.
 */
public interface QuerySpecification<T> extends Specification<T> {
    String toQueryFragment(); // ex: "status = 'CONFIRMED'"
}
