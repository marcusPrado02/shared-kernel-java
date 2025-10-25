package com.marcusprado02.sharedkernel.infrastructure.persistence.graph;

import java.util.*;

import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.*;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.EdgeSpec;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.GraphCriteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Pattern;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping.EdgeCommand;

public interface GraphRepository<E, ID> {
  // Vértices
  Optional<E> findById(ID id);
  E getById(ID id);
  List<E> findAll(GraphCriteria criteria);
  Page<E> findPage(GraphCriteria criteria, PageRequest page);
  SeekPage<E> findPageBySeek(GraphCriteria c, Sort sort, Optional<SeekKey> after, int limit);
  boolean exists(GraphCriteria c);
  long count(GraphCriteria c);

  E insert(E entity);                 // cria vértice
  E update(E entity, long expectedVersion); // CAS -> optimistic lock
  E upsert(E entity);                 // idempotente por ID
  void deleteById(ID id);             // soft/hard conforme adapter
  void delete(GraphCriteria criteria);

  // Arestas
  void link(EdgeCommand edge);        // addE/MERGE REL
  void unlink(EdgeCommand edge);      // dropE/DETACH DELETE REL
  List<E> neighbours(Object id, EdgeSpec edgeSpec, GraphCriteria filter);

  // Caminhos
  <T> List<T> findPaths(Pattern pattern, int limit, Class<T> projectionType);
  com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Page<E> findPage(GraphCriteria c,
      com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.PageRequest p);
  com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.SeekPage<E> findPageBySeek(GraphCriteria c,
        com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Sort sort,
        Optional<com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.SeekKey> after, int limit);
}
