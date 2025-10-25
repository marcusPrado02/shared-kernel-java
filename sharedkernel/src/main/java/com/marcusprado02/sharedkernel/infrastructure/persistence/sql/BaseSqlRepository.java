package com.marcusprado02.sharedkernel.infrastructure.persistence.sql;

import org.springframework.jdbc.core.namedparam.*;
import org.springframework.transaction.support.TransactionTemplate;

import com.marcusprado02.sharedkernel.domain.model.base.Versioned;
import com.marcusprado02.sharedkernel.domain.repository.errors.NotFoundException;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Criteria;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Filter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Op;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Page;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.PageRequest;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekKey;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.SeekPage;
import com.marcusprado02.sharedkernel.infrastructure.persistence.criteria.Sort;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.dialect.SqlDialect;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.exceptions.OptimisticLockException;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.mapping.RowMapperExt;
import com.marcusprado02.sharedkernel.infrastructure.persistence.sql.mapping.RowUnmapper;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import java.util.*;
import java.util.stream.*;

public abstract class BaseSqlRepository<E extends Versioned, ID>
  implements SqlRepository<E, ID> {

  protected final NamedParameterJdbcTemplate jdbc;
  protected final TransactionTemplate tx;
  protected final SqlDialect dialect;
  protected final String table;
  protected final String idColumn;
  protected final String versionColumn; // ex: "version"
  protected final boolean softDelete;
  protected final Optional<String> deletedAtColumn; // ex: "deleted_at"
  protected final Optional<String> tenantColumn;    // ex: "tenant_id"

  protected final RowMapperExt<E> rowMapper;
  protected final RowUnmapper<E> rowUnmapper;
  protected final Set<String> updatableColumns; // sem id/version/tenant/deleted_at

  protected BaseSqlRepository(
      NamedParameterJdbcTemplate jdbc,
      TransactionTemplate tx,
      SqlDialect dialect,
      String table,
      String idColumn,
      String versionColumn,
      boolean softDelete,
      Optional<String> deletedAtColumn,
      Optional<String> tenantColumn,
      RowMapperExt<E> rowMapper,
      RowUnmapper<E> rowUnmapper,
      Set<String> updatableColumns) {
    this.jdbc = jdbc;
    this.tx = tx;
    this.dialect = dialect;
    this.table = table;
    this.idColumn = idColumn;
    this.versionColumn = versionColumn;
    this.softDelete = softDelete;
    this.deletedAtColumn = deletedAtColumn;
    this.tenantColumn = tenantColumn;
    this.rowMapper = rowMapper;
    this.rowUnmapper = rowUnmapper;
    this.updatableColumns = Set.copyOf(updatableColumns);
  }

  // === Query helpers =======================================================

  protected String selectBase() {
    return "SELECT * FROM " + dialect.q(table);
  }

  protected String where(Criteria c, Map<String,Object> params) {
    var clauses = new ArrayList<String>();
    for (int i=0; i<c.filters().size(); i++) {
      var f = c.filters().get(i);
      var pn = "p" + i;
      switch (f.op()) {
        case EQ -> { clauses.add(dialect.q(f.field()) + " = :" + pn); params.put(pn, f.value()); }
        case NE -> { clauses.add(dialect.q(f.field()) + " <> :" + pn); params.put(pn, f.value()); }
        case GT -> { clauses.add(dialect.q(f.field()) + " > :" + pn); params.put(pn, f.value()); }
        case GTE-> { clauses.add(dialect.q(f.field()) + " >= :" + pn); params.put(pn, f.value()); }
        case LT -> { clauses.add(dialect.q(f.field()) + " < :" + pn); params.put(pn, f.value()); }
        case LTE-> { clauses.add(dialect.q(f.field()) + " <= :" + pn); params.put(pn, f.value()); }
        case LIKE->{ clauses.add(dialect.q(f.field()) + " LIKE :" + pn); params.put(pn, f.value()); }
        case IN -> {
          @SuppressWarnings("unchecked")
          var list = (Collection<?>) f.value();
          clauses.add(dialect.q(f.field()) + " IN (:" + pn + ")");
          params.put(pn, list);
        }
        case BETWEEN -> {
          var arr = (List<?>) f.value();
          params.put(pn+"a", arr.get(0));
          params.put(pn+"b", arr.get(1));
          clauses.add(dialect.q(f.field()) + " BETWEEN :"+pn+"a AND :"+pn+"b");
        }
        case IS_NULL -> clauses.add(dialect.q(f.field()) + " IS NULL");
        case NOT_NULL-> clauses.add(dialect.q(f.field()) + " IS NOT NULL");
      }
    }
    // multi-tenant + soft delete guards
    tenantColumn.ifPresent(tc -> {
      if (!params.containsKey("_tenant")) {
        clauses.add(dialect.q(tc) + " = :_tenant");
      }
    });
    deletedAtColumn.ifPresent(dc -> clauses.add(dialect.q(dc) + " IS NULL"));
    return clauses.isEmpty() ? "" : (" WHERE " + String.join(" AND ", clauses));
  }

  protected String order(Criteria c) {
    return c.sort().map(s -> " ORDER BY " + dialect.q(s.field()) + (s.asc() ? " ASC":" DESC"))
                 .orElse("");
  }

  // === Reads ===============================================================

  @Override
  public Optional<E> findById(ID id) {
    var c = Criteria.of(new Filter(idColumn, Op.EQ, id));
    var list = findAll(c);
    return list.stream().findFirst();
  }

  @Override
  public E getById(ID id) {
    return findById(id).orElseThrow(() -> new NotFoundException(table+" id="+id));
  }

  @Override
  public List<E> findAll(Criteria criteria) {
    var params = new HashMap<String,Object>();
    var sql = selectBase() + where(criteria, params) + order(criteria);
    return jdbc.query(sql, withTenant(params), (rs, i) -> {
      try { return rowMapper.map(rs); } catch (Exception e) { throw new RuntimeException(e); }
    });
  }

  @Override
  public Page<E> findPage(Criteria c, PageRequest page) {
    var params = new HashMap<String,Object>();
    var where = where(c, params);
    var order = order(c);
    var limit = dialect.limitOffsetClause(page.size(), page.page()*page.size());
    var sql = selectBase() + where + order + limit;
    var content = jdbc.query(sql, withTenant(params), (rs, i) -> {
      try { return rowMapper.map(rs); } catch (Exception e) { throw new RuntimeException(e); }
    });
    var total = count(c);
    return new Page<>(content, total, page.page(), page.size());
  }

  @Override
  public SeekPage<E> findPageBySeek(Criteria c, Sort sort, Optional<SeekKey> after, int limit) {
    // assume sort by single indexed column for keyset
    var params = new HashMap<String,Object>();
    var baseWhere = new StringBuilder(where(c, params));
    after.ifPresent(sk -> {
      var v = sk.values()[0];
      params.put("_seek", v);
      var cond = dialect.q(sort.field()) + (sort.asc()? " > ":" < ") + ":_seek";
      baseWhere.append(baseWhere.length()==0? " WHERE ":" AND ").append(cond);
    });
    var order = " ORDER BY " + dialect.q(sort.field()) + (sort.asc()? " ASC ":" DESC ");
    var sql = selectBase() + baseWhere + order + dialect.limitOffsetClause(limit, 0);
    var rows = jdbc.query(sql, withTenant(params), (rs, i) -> {
      try { return rowMapper.map(rs); } catch (Exception e) { throw new RuntimeException(e); }
    });
    var next = rows.isEmpty()? Optional.<SeekKey>empty()
                             : Optional.of(new SeekKey(new Object[]{ extractSortValue(rows.get(rows.size()-1), sort.field()) }));
    return new SeekPage<>(rows, next);
  }

  // Hook para extrair valor da ordenação (implemente no repo concreto se precisar)
  protected Object extractSortValue(E entity, String field){ return null; }

  @Override
  public boolean exists(Criteria c){ return count(c) > 0; }

  @Override
  public long count(Criteria c){
    var params = new HashMap<String,Object>();
    var sql = "SELECT COUNT(*) FROM " + dialect.q(table) + where(c, params);
    Long n = jdbc.queryForObject(sql, withTenant(params), Long.class);
    return n == null ? 0 : n;
  }

  // === Writes =============================================================

  @Override
  public E insert(E e) {
    var cols = rowUnmapper.toColumns(e);
    var keys = new MapSqlParameterSource(withTenant(cols));
    var colNames = cols.keySet();
    var sql = "INSERT INTO " + dialect.q(table) + " (" +
        colNames.stream().map(dialect::q).collect(Collectors.joining(",")) + ") VALUES (" +
        colNames.stream().map(dialect::placeholder).collect(Collectors.joining(",")) + ")";
    var updated = jdbc.update(sql, keys);
    if (updated != 1) throw new IllegalStateException("Insert affected "+updated+" rows");
    return e;
  }

  @Override
  public E update(E e) {
    var cols = new LinkedHashMap<>(rowUnmapper.toColumns(e));
    var idVal = cols.remove(idColumn);
    var verVal = cols.remove(versionColumn);
    var sets = updatableColumns.stream()
      .filter(cols::containsKey)
      .map(c -> dialect.q(c) + " = " + dialect.placeholder(c))
      .collect(Collectors.joining(", "));
    // optimistic lock
    var sql = "UPDATE " + dialect.q(table) + " SET " + sets + ", " + dialect.q(versionColumn) +
              " = " + dialect.q(versionColumn) + " + 1 WHERE " + dialect.q(idColumn) + " = :_id AND " +
              dialect.q(versionColumn) + " = :_ver";
    var params = new HashMap<>(withTenant(cols));
    params.put("_id", idVal);
    params.put("_ver", verVal);
    var n = jdbc.update(sql, params);
    if (n != 1) throw new OptimisticLockException(table+" id="+idVal+" version="+verVal);
    return e;
  }

  @Override
  public E upsert(E e) {
    var cols = rowUnmapper.toColumns(e);
    var names = cols.keySet().toArray(String[]::new);
    var allCols = String.join(",", Arrays.stream(names).map(dialect::q).toList());
    var values = String.join(",", Arrays.stream(names).map(dialect::placeholder).toList());
    var updatable = updatableColumns.toArray(String[]::new);
    var sql = "INSERT INTO " + dialect.q(table) + " ("+allCols+") VALUES ("+values+")"
            + dialect.upsert(table, new String[]{idColumn}, updatable);
    jdbc.update(sql, withTenant(cols));
    return e;
  }

  @Override
  public void deleteById(ID id) {
    if (softDelete && deletedAtColumn.isPresent()) {
      var sql = "UPDATE " + dialect.q(table) + " SET " + dialect.q(deletedAtColumn.get()) + " = NOW() " +
                "WHERE " + dialect.q(idColumn) + " = :id";
      jdbc.update(sql, Map.of("id", id));
    } else {
      var sql = "DELETE FROM " + dialect.q(table) + " WHERE " + dialect.q(idColumn) + " = :id";
      jdbc.update(sql, Map.of("id", id));
    }
  }

  @Override
  public void delete(Criteria c) {
    var params = new HashMap<String,Object>();
    var w = where(c, params);
    var sql = softDelete && deletedAtColumn.isPresent()
        ? "UPDATE " + dialect.q(table) + " SET " + dialect.q(deletedAtColumn.get()) + " = NOW() " + w
        : "DELETE FROM " + dialect.q(table) + w;
    jdbc.update(sql, withTenant(params));
  }

  @Override
  public int[] batchInsert(List<E> list) {
    var batch = list.stream().map(rowUnmapper::toColumns).map(this::withTenant)
        .map(MapSqlParameterSource::new).toArray(MapSqlParameterSource[]::new);
    var first = rowUnmapper.toColumns(list.get(0));
    var names = first.keySet();
    var sql = "INSERT INTO " + dialect.q(table) + " (" +
        names.stream().map(dialect::q).collect(Collectors.joining(",")) + ") VALUES (" +
        names.stream().map(dialect::placeholder).collect(Collectors.joining(",")) + ")";
    return jdbc.batchUpdate(sql, batch);
  }

  @Override
  public int[] batchUpdate(List<E> list) {
    return list.stream().map(this::update).mapToInt(e -> 1).toArray();
  }

  // === Tx & helpers ========================================================

  @Override
  public void withTransaction(Runnable work) { tx.executeWithoutResult(s -> work.run()); }

  protected Map<String,Object> withTenant(Map<String,Object> params){
    if (tenantColumn.isPresent() && !params.containsKey("_tenant")) {
      params = new HashMap<>(params);
      params.put("_tenant", currentTenant());
    }
    return params;
  }

  protected String currentTenant(){ return TenantProviderHolder.provider().tenantId(); }

  // Static holder para evitar dependência circular
  public static final class TenantProviderHolder {
    private static TenantProvider provider;
    public static void set(TenantProvider p){ provider = p; }
    public static TenantProvider provider(){ return provider; }
  }
}

