package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.adapter.gremlin;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.GraphRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.EdgeSpec;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.GraphCriteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.NodeFilter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Pattern;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Page;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.PageRequest;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.Sort;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.SeekKey;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.SeekPage;

import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping.GraphEntityMapper;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping.EdgeCommand;

import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.NotFoundException;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.OptimisticLockException;

public abstract class GremlinGraphRepository<E, ID> implements GraphRepository<E, ID> {

  protected final Cluster cluster;
  protected final Client client;              // sessões remotas se suportado
  protected final GraphTraversalSource g;     // traversal source (pode ser remoto)
  protected final GraphEntityMapper<E> mapper;
  protected final TenantProvider tenantProvider;
  protected final boolean softDelete;

  protected GremlinGraphRepository(Cluster cluster,
                                   GraphTraversalSource g,
                                   GraphEntityMapper<E> mapper,
                                   TenantProvider tenantProvider,
                                   boolean softDelete) {
    this.cluster = cluster;
    this.client = cluster.connect();
    this.g = g;
    this.mapper = mapper;
    this.tenantProvider = tenantProvider;
    this.softDelete = softDelete;
  }

  protected String tenant(){ return tenantProvider.tenantId(); }
  protected String L(){ return mapper.vertexLabel(); }
  protected String ID(){ return mapper.idProperty(); }
  protected String VER(){ return mapper.versionProperty(); }

  // ------------------------------- VÉRTICES --------------------------------

  @Override
  public Optional<E> findById(ID id) {
    GraphTraversal<Vertex, Vertex> t = baseV().has(ID(), id);
    if (softDelete) t = t.has("deletedAt", P.eq(null));  
    return t.limit(1).valueMap(true).tryNext().map(this::toEntity);
  }

  @Override
  public E getById(ID id) { return findById(id).orElseThrow(() -> new NotFoundException(L()+" id="+id)); }

  @Override
  public List<E> findAll(GraphCriteria c) {
    GraphTraversal<Vertex, Vertex> t = baseV().hasLabel(c.label());
    t = applyNodeFilters(t, c.nodeFilters());
    if (c.pattern().isPresent()) t = applyPattern(t, c.pattern().get());
    t = applySort(t, c.sort());
    return t.valueMap(true).toList().stream().map(this::toEntity).toList();
  }

  @Override
  public Page<E> findPage(GraphCriteria c, PageRequest p) {
    long total = count(c);
    GraphTraversal<Vertex, Vertex> t = baseV().hasLabel(c.label());
    t = applyNodeFilters(t, c.nodeFilters());
    if (c.pattern().isPresent()) t = applyPattern(t, c.pattern().get());
    t = applySort(t, c.sort()).range((long)p.page()*p.size(), (long)p.page()*p.size()+p.size());
    List<E> res = t.valueMap(true).toList().stream().map(this::toEntity).toList();
    return new Page<>(res, total, p.page(), p.size());
  }

  @Override
  public SeekPage<E> findPageBySeek(GraphCriteria c, Sort sort, Optional<SeekKey> after, int limit) {
    GraphTraversal<Vertex, Vertex> t = baseV().hasLabel(c.label());
    t = applyNodeFilters(t, c.nodeFilters());
    if (c.pattern().isPresent()) t = applyPattern(t, c.pattern().get());

    t = sort.asc() ? t.order().by(sort.field()) : t.order().by(sort.field(), Order.desc);

    if (after.isPresent()) {
      Object v = after.get().values()[0];
      t = sort.asc() ? t.has(sort.field(), P.gt(v)) : t.has(sort.field(), P.lt(v));
    }

    List<E> rows = t.limit(limit).valueMap(true).toList().stream().map(this::toEntity).toList();

    Optional<SeekKey> next = rows.isEmpty()
        ? Optional.empty()
        : Optional.of(new SeekKey(new Object[]{ extractSortValue(rows.get(rows.size()-1), sort.field()) }));

    return new SeekPage<>(rows, next);
  }

  @Override
  public boolean exists(GraphCriteria c) { return count(c) > 0; }

  @Override
  public long count(GraphCriteria c) {
    GraphTraversal<Vertex, Vertex> t = baseV().hasLabel(c.label());
    t = applyNodeFilters(t, c.nodeFilters());
    if (c.pattern().isPresent()) t = applyPattern(t, c.pattern().get());
    return t.count().next();
  }

  @Override
  public E insert(E e) {
    Map<String,Object> props = new LinkedHashMap<>(mapper.toProperties(e));
    props.putIfAbsent("tenantId", tenant());
    if (softDelete) props.putIfAbsent("deletedAt", null);
    props.putIfAbsent(VER(), 0L);
    GraphTraversal<Vertex, Vertex> t = g.addV(L());
    for (var en : props.entrySet()) t = t.property(en.getKey(), en.getValue());
    t.iterate();
    return e;
  }

  @Override
  public E update(E e, long expectedVersion) {
    Map<String,Object> props = mapper.toProperties(e);
    Object id = props.get(ID());

    GraphTraversal<Vertex, Vertex> check = baseV().has(ID(), id).has(VER(), expectedVersion);
    if (softDelete) check = check.has("deletedAt", P.eq(null));
    if (!check.hasNext()) throw new OptimisticLockException(L()+" id="+id+" version="+expectedVersion);

    GraphTraversal<Vertex, Vertex> vt = baseV().has(ID(), id);
    for (var en : props.entrySet()) {
      if (en.getKey().equals(VER())) continue;
      vt = vt.property(en.getKey(), en.getValue());
    }
    vt.property(VER(), expectedVersion + 1).iterate();
    return e;
  }

  @Override
  public E upsert(E e) {
    Map<String,Object> props = mapper.toProperties(e);
    Object id = props.get(ID());
    GraphTraversal<Vertex, Vertex> t = baseV().has(ID(), id);
    if (t.hasNext()) {
      Object current = t.next().value(VER());
      return update(e, ((Number) current).longValue());
    } else {
      return insert(e);
    }
  }

  @Override
  public void deleteById(ID id) {
    GraphTraversal<Vertex, Vertex> t = baseV().has(ID(), id);
    if (softDelete) {
      t.property("deletedAt", Instant.now()).iterate();
    } else {
      t.drop().iterate();
    }
  }

  @Override
  public void delete(GraphCriteria c) {
    GraphTraversal<Vertex, Vertex> t = baseV().hasLabel(c.label());
    t = applyNodeFilters(t, c.nodeFilters());
    if (softDelete) t.property("deletedAt", Instant.now()).iterate();
    else t.drop().iterate();
  }

  // ------------------------------- ARESTAS ---------------------------------

  @Override
  public void link(EdgeCommand edge) {
    GraphTraversal<Vertex, Vertex> from = baseV().has(ID(), edge.fromId());
    GraphTraversal<Vertex, Vertex> to   = baseV().has(ID(), edge.toId());
    var e = from.addE(edge.edgeLabel()).to(to).property("tenantId", tenant());
    if (softDelete) e = e.property("deletedAt", null);
    for (var en : edge.properties().entrySet()) e = e.property(en.getKey(), en.getValue());
    e.iterate();
  }

  @Override
  public void unlink(EdgeCommand edge) {
    GraphTraversal<Vertex, ?> t = baseV().has(ID(), edge.fromId())
        .bothE(edge.edgeLabel()).where(otherV().has(ID(), edge.toId()).has("tenantId", tenant()));
    if (softDelete) t = t.property("deletedAt", Instant.now());
    else t = t.drop();
    t.iterate();
  }

  @Override
  public List<E> neighbours(Object id, EdgeSpec spec, GraphCriteria filter) {
    GraphTraversal<Vertex, Vertex> t = baseV().has(ID(), id);
    switch (spec.direction()) {
      case OUT  -> t = t.outE(spec.label()).has("tenantId", tenant()).has("deletedAt", P.eq(null)).inV();
      case IN   -> t = t.inE(spec.label()).has("tenantId", tenant()).has("deletedAt", P.eq(null)).outV();
      case BOTH -> t = t.bothE(spec.label()).has("tenantId", tenant()).has("deletedAt", P.eq(null)).otherV();
    }
    t = t.hasLabel(filter.label());
    t = applyNodeFilters(t, filter.nodeFilters());
    if (filter.sort().isPresent()) t = applySort(t, filter.sort());
    return t.valueMap(true).toList().stream().map(this::toEntity).toList();
  }

  // ------------------------------- PATHS -----------------------------------

  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> findPaths(Pattern pattern, int limit, Class<T> projectionType) {
    GraphTraversal<Vertex, ?> trav = baseV(); // V do tenant + softDelete

    for (var es : pattern.chain()) {
      switch (es.direction()) {
        case OUT  -> trav = ((GraphTraversal<Vertex, ?>) trav).repeat((org.apache.tinkerpop.gremlin.process.traversal.Traversal) out(es.label())).times(es.maxDepth());
        case IN   -> trav = ((GraphTraversal<Vertex, ?>) trav).repeat((org.apache.tinkerpop.gremlin.process.traversal.Traversal) in (es.label())).times(es.maxDepth());
        case BOTH -> trav = ((GraphTraversal<Vertex, ?>) trav).repeat((org.apache.tinkerpop.gremlin.process.traversal.Traversal) both(es.label())).times(es.maxDepth());
      }
    }

    // path -> lista
    List<Path> paths = ((GraphTraversal<Vertex, Path>) ((GraphTraversal<Vertex, ?>) trav).path().limit(limit)).toList();

    List<T> result = new ArrayList<>(paths.size());
    for (Path p : paths) {
      @SuppressWarnings("unchecked")
      T dto = (T) Map.of("path", p);
      result.add(dto);
    }
    return result;
  }

  // ----------------------------- HELPERS -----------------------------------

  protected GraphTraversal<Vertex, Vertex> baseV(){
    GraphTraversal<Vertex, Vertex> t = g.V().has("tenantId", tenant());
    if (softDelete) t = t.has("deletedAt", P.eq(null)); 
    return t;
  }

  protected <S> GraphTraversal<S, Vertex> applyNodeFilters(GraphTraversal<S, Vertex> t, List<NodeFilter> fs){
    for (NodeFilter f : fs) {
      t = t.has(f.field(), toGremlinPredicate(f));
    }
    return t;
  }

  private P<?> toGremlinPredicate(NodeFilter f){
    Object v = f.value();
    return switch (f.op()) {
      case EQ       -> P.eq(v);
      case NE       -> P.neq(v);
      case GT       -> P.gt(v);
      case GTE      -> P.gte(v);
      case LT       -> P.lt(v);
      case LTE      -> P.lte(v);
      case LIKE     -> P.test((a,b)->String.valueOf(a).toLowerCase().contains(String.valueOf(b).toLowerCase()), v);
      case IN       -> P.within((Collection<?>) v);
      case BETWEEN  -> { var l = (List<?>) v; yield P.between(l.get(0), l.get(1)); }
      case IS_NULL  -> P.eq(null);
      case NOT_NULL -> P.neq(null);
    };
  }

  protected GraphTraversal<Vertex, Vertex> applyPattern(GraphTraversal<Vertex, Vertex> t, Pattern p){
    for (var es : p.chain()) {
      switch (es.direction()) {
        case OUT  -> t = t.repeat(out(es.label())).times(es.maxDepth());
        case IN   -> t = t.repeat(in (es.label())).times(es.maxDepth());
        case BOTH -> t = t.repeat(both(es.label())).times(es.maxDepth());
      }
    }
    return t;
  }

  protected GraphTraversal<Vertex, Vertex> applySort(GraphTraversal<Vertex, Vertex> t, Optional<Sort> s){
    return s.map(ss -> ss.asc() ? t.order().by(ss.field())
                                : t.order().by(ss.field(), Order.desc))
            .orElse(t);
  }

  protected Object extractSortValue(E entity, String field){
    try {
      var m = entity.getClass().getMethod("get" + Character.toUpperCase(field.charAt(0)) + field.substring(1));
      return m.invoke(entity);
    } catch (Exception e) { throw new IllegalArgumentException("Sort field inválido: "+field, e); }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  protected E toEntity(Map<Object,Object> valueMapTrue){
    // valueMap(true) traz id e label(s)
    Map<String,Object> props = valueMapTrue.entrySet().stream()
      .collect(Collectors.toMap(
        e -> String.valueOf(e.getKey()),
        e -> (e.getValue() instanceof List<?> l && l.size()==1) ? l.get(0) : e.getValue()
      ));
    return (E) mapper.fromProperties((Map) props);
  }
}
