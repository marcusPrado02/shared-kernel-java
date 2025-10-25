package com.marcusprado02.sharedkernel.infrastructure.persistence.sql;


import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.*;

public interface SqlRepository<E, ID> {
  Optional<E> findById(ID id);
  E getById(ID id); // NotFoundException
  List<E> findAll(Criteria criteria);
  Page<E> findPage(Criteria criteria, PageRequest page);
  SeekPage<E> findPageBySeek(Criteria criteria, Sort sort, Optional<SeekKey> after, int limit);
  boolean exists(Criteria criteria);
  long count(Criteria criteria);

  E insert(E entity);
  E update(E entity);                 // optimistic lock
  E upsert(E entity);                 // dialect-specific
  void deleteById(ID id);             // hard or soft (configurável)
  void delete(Criteria criteria);
  int[] batchInsert(List<E> entities);
  int[] batchUpdate(List<E> entities);

  // Low-level hooks
  void withTransaction(Runnable work);
}

