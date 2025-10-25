package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.mapping;
import java.util.Map;
public interface RowUnmapper<E> { Map<String, Object> toColumns(E entity); }
