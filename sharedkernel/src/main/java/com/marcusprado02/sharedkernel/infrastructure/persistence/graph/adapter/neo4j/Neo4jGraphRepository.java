package com.marcusprado02.sharedkernel.infrastructure.persistence.graph.adapter.neo4j;


import org.springframework.data.neo4j.core.Neo4jClient;

import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.GraphRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.criteria.*;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping.EdgeCommand;
import com.marcusprado02.sharedkernel.infrastructure.persistence.graph.mapping.GraphEntityMapper;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.OptimisticLockException;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import java.time.Instant;
import java.util.*;

public abstract class Neo4jGraphRepository<E, ID> implements GraphRepository<E, ID> {

  protected final Neo4jClient neo4j;
  protected final GraphEntityMapper<E> mapper;
  protected final TenantProvider tenantProvider;
  protected final boolean softDelete;

  protected Neo4jGraphRepository(Neo4jClient client, GraphEntityMapper<E> mapper,
                                 TenantProvider tenantProvider, boolean softDelete){
    this.neo4j = client;
    this.mapper = mapper;
    this.tenantProvider = tenantProvider;
    this.softDelete = softDelete;
  }

  protected String T(){ return tenantProvider.tenantId(); }
  protected String L(){ return mapper.vertexLabel(); }
  protected String ID(){ return mapper.idProperty(); }
  protected String VER(){ return mapper.versionProperty(); }

  @Override
  public Optional<E> findById(ID id) {
    var cy = new StringBuilder("MATCH (n:"+L()+" {"+ID()+": $id, tenantId: $tenant}) ");
    if (softDelete) cy.append("WHERE n.deletedAt IS NULL ");
    cy.append("RETURN n");
    return neo4j.query(cy.toString())
      .bindAll(Map.of("id", id, "tenant", T()))
      .fetchAs(Map.class)
      .mappedBy((t, r) -> (Map<String,Object>) r.get("n"))
      .one()
      .map(mapper::fromProperties);
  }

  @Override public E getById(ID id){ 
    return findById(id).orElseThrow(() -> new NoSuchElementException(L()+" id="+id));
  }

  @Override
  public List<E> findAll(GraphCriteria c) {
    var p = new HashMap<String,Object>();
    var cy = buildMatch(c, p) + " RETURN n" + buildOrder(c.sort());
    return neo4j.query(cy).bindAll(p).fetchAs(Map.class)
      .mappedBy((t, r) -> (Map<String,Object>) r.get("n"))
      .all().stream().map(mapper::fromProperties).toList();
  }

  public Page<E> findPage(GraphCriteria c, PageRequest page) {
    var p = new HashMap<String,Object>();
    var match = buildMatch(c, p);
    var list = neo4j.query(match + " RETURN n" + buildOrder(c.sort()) + " SKIP $skip LIMIT $limit")
      .bindAll(p).bind(page.page()*page.size()).to("skip").bind(page.size()).to("limit")
      .fetchAs(Map.class).mappedBy((t, r) -> (Map<String,Object>) r.get("n")).all()
      .stream().map(mapper::fromProperties).toList();
    var total = neo4j.query(match + " RETURN count(n) as cnt").bindAll(p).fetchAs(Long.class).one().orElse(0L);
    return new Page<>(list, total, page.page(), page.size());
  }

  public SeekPage<E> findPageBySeek(GraphCriteria c, Sort sort, Optional<SeekKey> after, int limit) {
    var p = new HashMap<String,Object>();
    var match = buildMatch(c, p);
    var order = sort.asc()? "ASC":"DESC";
    var whereSeek = after.map(sk -> " AND n."+sort.field()+" "+(sort.asc()?">": "<")+" $seek").orElse("");
    after.ifPresent(sk -> p.put("seek", sk.values()[0]));
    var cy = match + " AND true " + whereSeek + " RETURN n ORDER BY n."+sort.field()+" "+order+" LIMIT $limit";
    p.put("limit", limit);
    var propsList = neo4j.query(cy).bindAll(p).fetchAs(Map.class)
      .mappedBy((t,r)->(Map<String,Object>) r.get("n")).all().stream().toList();
    var list = propsList.stream().map(mapper::fromProperties).toList();
    var next = propsList.isEmpty()? Optional.<SeekKey>empty()
                                 : Optional.of(new SeekKey(new Object[]{ 
                                  extract(propsList.get(propsList.size()-1), sort.field()) 
                              }));
    return new SeekPage<>(list, next);
  }

  @Override public boolean exists(GraphCriteria c){ return count(c) > 0; }

  @Override
  public long count(GraphCriteria c) {
    var p = new HashMap<String,Object>();
    var match = buildMatch(c, p);
    return neo4j.query(match + " RETURN count(n) as cnt").bindAll(p).fetchAs(Long.class).one().orElse(0L);
  }

  @Override
  public E insert(E e) {
    var props = new LinkedHashMap<>(mapper.toProperties(e));
    props.putIfAbsent("tenantId", T());
    if (softDelete) props.putIfAbsent("deletedAt", null);
    props.putIfAbsent(VER(), 0L);
    var cy = "CREATE (n:"+L()+" $p) RETURN n";
    neo4j.query(cy).bind(props).to("p").fetch().one();
    return e;
  }

  @Override
  public E update(E e, long expectedVersion) {
    var props = new LinkedHashMap<>(mapper.toProperties(e));
    var id = props.get(ID());
    props.remove(VER());
    var set = props.keySet().stream().map(k -> "n."+k+" = $p."+k).reduce((a,b)->a+", "+b).orElse("");
    var cy = "MATCH (n:"+L()+" {"+ID()+": $id, tenantId: $tenant, "+VER()+": $ver}) "
           + (softDelete? "WHERE n.deletedAt IS NULL ":"")
           + "SET "+set+", n."+VER()+" = n."+VER()+" + 1 RETURN n";
    var ok = neo4j.query(cy).bind(id).to("id").bind(T()).to("tenant").bind(expectedVersion).to("ver").bind(props).to("p")
      .fetchAs(Map.class).mappedBy((t,r)->(Map<String,Object>) r.get("n")).one();
    if (ok.isEmpty()) throw new OptimisticLockException(L()+" id="+id+" version="+expectedVersion);
    return e;
  }

  @Override
  public E upsert(E e) {
    var props = new LinkedHashMap<>(mapper.toProperties(e));
    props.putIfAbsent("tenantId", T());
    if (softDelete) props.putIfAbsent("deletedAt", null);
    props.putIfAbsent(VER(), 0L);
    var cy = "MERGE (n:"+L()+" {"+ID()+": $p."+ID()+", tenantId: $p.tenantId}) "
           + "ON CREATE SET n = $p "
           + "ON MATCH SET n += apoc.map.removeKeys($p, ['"+VER()+"']), n."+VER()+" = n."+VER()+" + 1 "
           + "RETURN n";
    neo4j.query(cy).bind(props).to("p").fetch().one();
    return e;
  }

  @Override
  public void deleteById(ID id) {
    if (softDelete) {
      var cy = "MATCH (n:"+L()+" {"+ID()+": $id, tenantId: $tenant}) SET n.deletedAt = $now";
      neo4j.query(cy).bind(id).to("id").bind(T()).to("tenant").bind(Instant.now()).to("now").run();
    } else {
      var cy = "MATCH (n:"+L()+" {"+ID()+": $id, tenantId: $tenant}) DETACH DELETE n";
      neo4j.query(cy).bind(id).to("id").bind(T()).to("tenant").run();
    }
  }

  @Override
  public void delete(GraphCriteria c) {
    var p = new HashMap<String,Object>();
    var match = buildMatch(c, p);
    if (softDelete) neo4j.query(match + " SET n.deletedAt = $now").bindAll(p).bind(Instant.now()).to("now").run();
    else neo4j.query(match + " DETACH DELETE n").bindAll(p).run();
  }

  @Override
  public void link(EdgeCommand edge) {
    var cy = "MATCH (a:"+L()+"), (b:"+L()+") "
           + "WHERE a."+ID()+" = $from AND a.tenantId = $t AND b."+ID()+" = $to AND b.tenantId = $t "
           + "MERGE (a)-[r:"+edge.edgeLabel()+" {tenantId: $t}]->(b) "
           + (softDelete? "ON CREATE SET r.deletedAt = null ":"")
           + "SET r += $props";
    neo4j.query(cy).bind(edge.fromId()).to("from").bind(edge.toId()).to("to").bind(T()).to("t")
         .bind(edge.properties()).to("props").run();
  }

  @Override
  public void unlink(EdgeCommand edge) {
    if (softDelete) {
      var cy = "MATCH (a {"+ID()+": $from, tenantId: $t})-[r:"+edge.edgeLabel()+"]->(b {"+ID()+": $to, tenantId: $t}) "
             + "SET r.deletedAt = $now";
      neo4j.query(cy).bind(edge.fromId()).to("from").bind(edge.toId()).to("to")
           .bind(T()).to("t").bind(Instant.now()).to("now").run();
    } else {
      var cy = "MATCH (a {"+ID()+": $from, tenantId: $t})-[r:"+edge.edgeLabel()+"]->(b {"+ID()+": $to, tenantId: $t}) "
             + "DELETE r";
      neo4j.query(cy).bind(edge.fromId()).to("from").bind(edge.toId()).to("to").bind(T()).to("t").run();
    }
  }

  @Override
  public List<E> neighbours(Object id, EdgeSpec edge, GraphCriteria filter) {
    var dir = switch (edge.direction()) { case OUT -> ">"; case IN -> "<"; case BOTH -> ""; };
    var rel = "-[r:"+edge.label()+" {tenantId: $t}]-";
    var arrow = switch (edge.direction()) {
      case OUT  -> "-[r:"+edge.label()+" {tenantId: $t}]->";
      case IN   -> "<-[r:"+edge.label()+" {tenantId: $t}]-";
      case BOTH -> "-[r:"+edge.label()+" {tenantId: $t}]-";
    };

    var where = new StringBuilder(" WHERE a."+ID()+" = $id AND a.tenantId = $t ");
    if (softDelete) where.append(" AND coalesce(r.deletedAt, null) IS NULL AND coalesce(n.deletedAt, null) IS NULL ");

    var p = new HashMap<String,Object>();
    p.put("id", id); p.put("t", T());

    var cy = "MATCH (a:"+L()+")"+arrow+"(n:"+filter.label()+") " + where + buildNodeFilters("n", filter.nodeFilters(), p)
           + " RETURN n" + buildOrder(filter.sort());
    return neo4j.query(cy).bindAll(p).fetchAs(Map.class)
      .mappedBy((t,r)->(Map<String,Object>) r.get("n")).all().stream().map(mapper::fromProperties).toList();
  }

  @Override
  public <T> List<T> findPaths(Pattern pattern, int limit, Class<T> projectionType) {
    // exemplo simples: primeiro edge do pattern como shape
    if (pattern.chain().isEmpty()) return List.of();
    var es = pattern.chain().get(0);
    var arrow = switch (es.direction()) {
      case OUT -> "-[r:"+es.label()+"]->";
      case IN  -> "<-[r:"+es.label()+"]-";
      case BOTH-> "-[r:"+es.label()+"]-";
    };
    var cy = "MATCH p = (a:"+L()+")"+arrow+"(b) WHERE a.tenantId = $t RETURN p LIMIT $limit";
    return neo4j.query(cy).bind(T()).to("t").bind(limit).to("limit")
      .fetchAs(Object.class).all().stream().map(projectionType::cast).toList();
  }

  // --------------------------- helpers -------------------------------------

  private String buildMatch(GraphCriteria c, Map<String,Object> p){
    var sb = new StringBuilder("MATCH (n:"+c.label()+") WHERE n.tenantId = $t ");
    p.put("t", T());
    if (softDelete) sb.append(" AND n.deletedAt IS NULL ");
    sb.append(buildNodeFilters("n", c.nodeFilters(), p));
    // (Opcional) pattern: expanda com MATCH adicional por chain
    return sb.toString();
  }

  private String buildNodeFilters(String alias, List<NodeFilter> fs, Map<String,Object> p){
    var i = 0; var w = new StringBuilder();
    for (var f : fs) {
      var key = "p"+(i++);
      w.append(" AND ");
      switch (f.op()){
        case EQ -> { w.append(alias).append(".").append(f.field()).append(" = $").append(key); p.put(key, f.value()); }
        case NE -> { w.append(alias).append(".").append(f.field()).append(" <> $").append(key); p.put(key, f.value()); }
        case GT -> { w.append(alias).append(".").append(f.field()).append(" > $").append(key); p.put(key, f.value()); }
        case GTE-> { w.append(alias).append(".").append(f.field()).append(" >= $").append(key); p.put(key, f.value()); }
        case LT -> { w.append(alias).append(".").append(f.field()).append(" < $").append(key); p.put(key, f.value()); }
        case LTE-> { w.append(alias).append(".").append(f.field()).append(" <= $").append(key); p.put(key, f.value()); }
        case LIKE -> { w.append("toLower(").append(alias).append(".").append(f.field()).append(") CONTAINS toLower($").append(key).append(")"); p.put(key, f.value()); }
        case IN -> { w.append(alias).append(".").append(f.field()).append(" IN $").append(key); p.put(key, f.value()); }
        case BETWEEN -> { var l = (List<?>) f.value(); var k1 = key + "a"; var k2 = key + "b";
                          w.append(alias).append(".").append(f.field()).append(" >= $").append(k1)
                           .append(" AND ").append(alias).append(".").append(f.field()).append(" <= $").append(k2);
                          p.put(k1, l.get(0)); p.put(k2, l.get(1)); }
        case IS_NULL -> { w.append(alias).append(".").append(f.field()).append(" IS NULL"); }
        case NOT_NULL-> { w.append(alias).append(".").append(f.field()).append(" IS NOT NULL"); }
      }
    }
    return w.toString();
  }

  private String buildOrder(Optional<Sort> s){
    return s.map(ss -> " ORDER BY n."+ss.field()+" "+(ss.asc()? "ASC":"DESC")).orElse("");
  }

  private Object extract(Map<String,Object> props, String field){ return props.get(field); }
}

