package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.adapter.timescale;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.TimeSeriesRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.*;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.Agg;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.Range;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.TagFilter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.TsQuery;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class TimescaleTimeSeriesRepository implements TimeSeriesRepository {

  private final NamedParameterJdbcTemplate jdbc;
  private final TenantProvider tenant;
  private final MeterRegistry meter;

  public TimescaleTimeSeriesRepository(NamedParameterJdbcTemplate jdbc, TenantProvider tenant, MeterRegistry meter) {
    this.jdbc = jdbc; this.tenant = tenant; this.meter = meter;
  }

  @Override
  public void write(Point p) {
    writeBatch(List.of(p));
  }

  @Override
  public void writeBatch(List<Point> points) {
    var sql = """
      INSERT INTO ts_samples (tenant_id, measurement, ts, tags, fields)
      VALUES (:tenant, :m, :ts, :tags::jsonb, :fields::jsonb)
      """;
    var batch = points.stream().map(p -> {
      var params = new HashMap<String,Object>();
      params.put("tenant", tenant.tenantId());
      params.put("m", p.measurement());
      params.put("ts", Timestamp.from(p.timestamp()));
      params.put("tags", toJson(withTenantTag(p.tags())));
      params.put("fields", toJson(p.fields().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> switch (e.getValue()) {
          case FieldValue.Num n  -> n.v();
          case FieldValue.Bool b -> b.v();
          case FieldValue.Str s  -> s.v();
        }))));
      return new MapSqlParameterSource(params);
    }).toArray(MapSqlParameterSource[]::new);
    Timer.Sample s = Timer.start(meter);
    jdbc.batchUpdate(sql, batch);
    s.stop(Timer.builder("ts.write").tag("backend","timescale").register(meter));
  }

  @Override
  public List<SeriesRow> query(TsQuery q) {
    var sb = new StringBuilder("""
      SELECT
        time_bucket(:bucket, ts) AS bucket_ts,
        tags,
        %s
      FROM ts_samples
      WHERE tenant_id = :tenant
        AND measurement = :m
        AND ts BETWEEN :from AND :to
        %s
      %s
      %s
      LIMIT :limit
      """);

    var selectAggs = q.aggregations().isEmpty()
      ? "jsonb_build_object('count', COUNT(*)) AS values"
      : q.aggregations().stream().map(a -> sqlAgg(a)).collect(Collectors.joining(", "));
    var tagWhere = buildTagFilterSql(q.tagFilters());
    var groupBy = buildGroupBy(q);
    var order = buildOrder(q);

    var sql = sb.toString().formatted(selectAggs, tagWhere.where(), groupBy, order);

    var params = new HashMap<String,Object>();
    params.put("tenant", tenant.tenantId());
    params.put("m", q.measurement());
    params.put("from", Timestamp.from(q.range().from()));
    params.put("to", Timestamp.from(q.range().to()));
    params.put("bucket", q.bucket().map(b -> Duration.between(Instant.EPOCH, Instant.EPOCH.plus(b.every())).toString()).orElse("interval '1 second'"));
    params.putAll(tagWhere.params());
    q.seekAfter().ifPresent(v -> params.put("seek", v));
    params.put("limit", q.limit());

    Timer.Sample s = Timer.start(meter);
    var rows = jdbc.query(sql, params, (rs, i) -> {
      var ts = rs.getTimestamp(1).toInstant();
      var tagsObj = parseJsonMap(rs.getString(2));
      var tags = tagsObj.entrySet().stream()
        .collect(Collectors.toMap(
          Map.Entry::getKey,
          e -> e.getValue() == null ? null : String.valueOf(e.getValue()),
          (a, b) -> b,
          LinkedHashMap::new));
      var values = parseJsonMap(rs.getString(3));
      return new SeriesRow(ts, tags, (Map<String,Object>) values);
    });
    s.stop(Timer.builder("ts.query").tag("backend","timescale").register(meter));
    return rows;
  }

  private static String sqlAgg(Agg a) {
    var expr = switch (a.fn()) {
      case MEAN -> "avg( (fields->>'%s')::double precision )".formatted(a.field());
      case SUM  -> "sum( (fields->>'%s')::double precision )".formatted(a.field());
      case MIN  -> "min( (fields->>'%s')::double precision )".formatted(a.field());
      case MAX  -> "max( (fields->>'%s')::double precision )".formatted(a.field());
      case COUNT-> "count(*)";
      case LAST -> "last( (fields->>'%s')::double precision, ts )".formatted(a.field());
      case FIRST-> "first( (fields->>'%s')::double precision, ts )".formatted(a.field());
      case P50  -> "percentile_cont(0.50) within group (order by (fields->>'%s')::double precision)".formatted(a.field());
      case P90  -> "percentile_cont(0.90) within group (order by (fields->>'%s')::double precision)".formatted(a.field());
      case P95  -> "percentile_cont(0.95) within group (order by (fields->>'%s')::double precision)".formatted(a.field());
      case P99  -> "percentile_cont(0.99) within group (order by (fields->>'%s')::double precision)".formatted(a.field());
    };
    return "%s AS %s".formatted(expr, a.as());
  }

  private record Where(String where, Map<String,Object> params) {}
  private Where buildTagFilterSql(List<TagFilter> fs) {
    var sb = new StringBuilder(); var p = new HashMap<String,Object>(); int i=0;
    for (var f : fs) {
      var key = "t"+(i++);
      sb.append(" AND ");
      switch (f.op()) {
        case EQ  -> { sb.append("tags->>:").append(key).append("k = :").append(key).append("v"); p.put(key+"k", f.key()); p.put(key+"v", f.value()); }
        case NEQ -> { sb.append("tags->>:").append(key).append("k <> :").append(key).append("v"); p.put(key+"k", f.key()); p.put(key+"v", f.value()); }
        case IN  -> { sb.append("tags->>:").append(key).append("k = ANY(:").append(key).append("arr)"); p.put(key+"k", f.key()); p.put(key+"arr", f.value()); }
        case NIN -> { sb.append("NOT (tags->>:").append(key).append("k = ANY(:").append(key).append("arr))"); p.put(key+"k", f.key()); p.put(key+"arr", f.value()); }
        case LIKE-> { sb.append("tags->>:").append(key).append("k ILIKE :").append(key).append("v"); p.put(key+"k", f.key()); p.put(key+"v", "%"+f.value()+"%"); }
        case PREFIX -> { sb.append("tags->>:").append(key).append("k LIKE :").append(key).append("v"); p.put(key+"k", f.key()); p.put(key+"v", f.value()+"%"); }
        case BETWEEN -> {
          if (!(f.value() instanceof List<?> l) || l.size()!=2) throw new IllegalArgumentException("BETWEEN requires a list of 2 values");
          sb.append("tags->>:").append(key).append("k BETWEEN :").append(key).append("v1 AND :").append(key).append("v2");
          p.put(key+"k", f.key()); p.put(key+"v1", l.get(0)); p.put(key+"v2", l.get(1));
        }
      }
    }
    return new Where(sb.toString(), p);
  }

  private String buildGroupBy(TsQuery q) {
    var gb = new StringBuilder("GROUP BY 1"); // bucket_ts
    for (var t : q.groupByTags()) gb.append(", (tags->>'").append(t).append("')");
    // SELECT retorna tags inteiras; opcionalmente pode projetar tags agrupadas
    return gb.toString();
  }

  private String buildOrder(TsQuery q) {
    var dir = q.orderDir()==TsQuery.SortDir.DESC ? "DESC" : "ASC";
    if (q.orderByField().orElse("time").equals("time")) {
      var seek = q.seekAfter().isPresent() ? (" AND ts "+(q.orderDir()==TsQuery.SortDir.ASC?">": "<")+" :seek ") : "";
      return seek + " ORDER BY 1 " + dir; // 1 => bucket_ts
    }
    return " ORDER BY 1 " + dir;
  }

  @Override public void ensureMeasurement(String m, Map<String,String> options) { /* schema já genérico */ }
  @Override public void setRetention(String m, Duration ttl) {
    jdbc.update("SELECT add_retention_policy('ts_samples', :ttl)", Map.of("ttl", "interval '"+ttl.toSeconds()+" seconds'"));
  }
  @Override public void dropRange(String m, Range r, Map<String,String> tags) {
    var params = new HashMap<String,Object>();
    params.put("tenant", tenant.tenantId());
    params.put("m", m);
    params.put("from", Timestamp.from(r.from()));
    params.put("to", Timestamp.from(r.to()));
    var sb = new StringBuilder("DELETE FROM ts_samples WHERE tenant_id=:tenant AND measurement=:m AND ts BETWEEN :from AND :to");
    int i=0;
    for (var e : tags.entrySet()) {
      var k = "k" + i;
      var v = "v" + i; 
      i++;
      sb.append(" AND tags->>:").append(k).append(" = :").append(v);
      params.put(k, e.getKey()); params.put(v, e.getValue());
    }
    jdbc.update(sb.toString(), params);
  }
  @Override public boolean ping() { return Boolean.TRUE.equals(jdbc.getJdbcTemplate().queryForObject("SELECT 1", Boolean.class)); }

  // helpers json
  private static String toJson(Object o){ try { return new ObjectMapper().writeValueAsString(o); } catch(Exception e){ throw new RuntimeException(e); } }
  @SuppressWarnings("unchecked")
  private static Map<String,Object> parseJsonMap(String s){ try { return new ObjectMapper().readValue(s, Map.class); } catch(Exception e){ throw new RuntimeException(e); } }
  private Map<String,String> withTenantTag(Map<String,String> tags){ var m=new LinkedHashMap<>(tags); m.putIfAbsent("tenantId", tenant.tenantId()); return m; }
}

