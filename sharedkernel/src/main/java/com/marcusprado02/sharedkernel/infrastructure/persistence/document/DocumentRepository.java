package com.marcusprado02.sharedkernel.infrastructure.persistence.document;

import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.*;

public interface DocumentRepository<E, ID> {
  Optional<E> findById(ID id);
  E getById(ID id);

  List<E> findAll(Criteria criteria);
  Page<E> findPage(Criteria criteria, PageRequest page);
  SeekPage<E> findPageBySeek(Criteria criteria, Sort sort, Optional<SeekKey> after, int limit);

  boolean exists(Criteria criteria);
  long count(Criteria criteria);

  E insert(E entity);
  E update(E entity);     // @Version para otimistic lock
  E upsert(E entity);     // save() semântica idempotente
  void deleteById(ID id); // soft/hard
  void delete(Criteria criteria);

  int[] batchInsert(List<E> entities);
  int[] batchUpdate(List<E> entities);

  void withTransaction(Runnable work); // para Cosmos/Mongo multi-doc: precisa de session/txn (Mongo >=4.0)
}
