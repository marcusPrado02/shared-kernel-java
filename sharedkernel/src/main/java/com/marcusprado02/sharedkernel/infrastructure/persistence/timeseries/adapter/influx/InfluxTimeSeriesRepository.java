package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.adapter.influx;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;

import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.TimeSeriesRepository;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.error.TsQueryException;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.error.TsWriteException;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.FieldValue;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.Point;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.SeriesRow;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.Range;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.TagFilter;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.TsQuery;
import com.marcusprado02.sharedkernel.infrastructure.tenancy.TenantProvider;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import reactor.util.retry.Retry;

public class InfluxTimeSeriesRepository implements TimeSeriesRepository {

  private final InfluxDBClient client;
  private final String org;
  private final String bucket;
  private final TenantProvider tenant;
  private final MeterRegistry meter;
  private final Retry retry; // se quiser usar em write assíncrono mais tarde

  public InfluxTimeSeriesRepository(InfluxDBClient client, String org, String bucket,
                                    TenantProvider tenant, MeterRegistry meter, Retry retry) {
    this.client = client; this.org = org; this.bucket = bucket; this.tenant = tenant; this.meter = meter; this.retry = retry;
  }

  @Override
  public void write(Point p) {
    writeBatch(List.of(p));
  }

  @Override
  public void writeBatch(List<Point> points) {
    WriteApiBlocking writer = client.getWriteApiBlocking();
    List<com.influxdb.client.write.Point> influxPoints = points.stream().map(this::toInfluxPoint).toList();
    Timer.Sample s = Timer.start(meter);
    try {
      writer.writePoints(bucket, org, influxPoints);
    } catch (Exception ex) {
      meter.counter("ts.write.error", "backend","influx").increment();
      throw new TsWriteException(ex);
    } finally {
      s.stop(Timer.builder("ts.write").tag("backend","influx").register(meter));
    }
  }

  private com.influxdb.client.write.Point toInfluxPoint(Point p) {
    Map<String,String> tags = new LinkedHashMap<>(p.tags());
    tags.putIfAbsent("tenantId", tenant.tenantId());

    com.influxdb.client.write.Point ip = com.influxdb.client.write.Point
      .measurement(p.measurement())
      .time(p.timestamp(), WritePrecision.NS);

    tags.forEach(ip::addTag);

    p.fields().forEach((k,v) -> {
      if (v instanceof FieldValue.Num n) ip.addField(k, n.v());
      else if (v instanceof FieldValue.Bool b) ip.addField(k, b.v());
      else if (v instanceof FieldValue.Str s) ip.addField(k, s.v());
    });
    return ip;
  }

  @Override
  public List<SeriesRow> query(TsQuery q) {
    QueryApi queryApi = client.getQueryApi();
    String flux = buildFlux(q);
    Timer.Sample s = Timer.start(meter);
    try {
      var tables = queryApi.query(flux, org);
      List<SeriesRow> rows = new ArrayList<>();
      tables.forEach(t -> t.getRecords().forEach(rec -> {
        Map<String,String> tags = new HashMap<>();
        rec.getValues().forEach((k,v) -> {
          if (k.startsWith("tag_")) tags.put(k.substring(4), v == null ? null : v.toString());
        });
        Map<String,Object> values = new LinkedHashMap<>();
        for (var agg : q.aggregations()) {
          String alias = agg.as();
          values.put(alias, rec.getValueByKey(alias));
        }
        rows.add(new SeriesRow(rec.getTime(), tags, values));
      }));
      return rows;
    } catch (Exception e) {
      throw new TsQueryException(e);
    } finally {
      s.stop(Timer.builder("ts.query").tag("backend","influx").register(meter));
    }
  }

  private String buildFlux(TsQuery q) {
    StringBuilder sb = new StringBuilder("""
      from(bucket:"%s")
        |> range(start: %s, stop: %s)
        |> filter(fn: (r) => r._measurement == "%s" and r.tenantId == "%s")
      """.formatted(bucket, q.range().from(), q.range().to(), q.measurement(), tenant.tenantId()));

    // filtros por tag (OBS: TagFilter.Op NÃO tem BETWEEN para TS)
    for (TagFilter f : q.tagFilters()) {
      String k = f.key();
      sb.append("|> filter(fn: (r) => ");
      sb.append(switch (f.op()) {
        case EQ  -> "r.%s == \"%s\"".formatted(k, f.value());
        case NEQ -> "r.%s != \"%s\"".formatted(k, f.value());
        case IN  -> {
          var list = ((Collection<?>)f.value()).stream().map(v->"\"%s\"".formatted(v)).toList();
          // simplificação: contém — ok para poucos valores; para muitos, construir várias ORs
          yield "contains(set: %s, value: r.%s)".formatted(list, k);
        }
        case NIN -> {
          var list = ((Collection<?>)f.value()).stream().map(v->"\"%s\"".formatted(v)).toList();
          yield "not contains(set: %s, value: r.%s)".formatted(list, k);
        }
        case LIKE, PREFIX -> "strings.hasPrefix(v: r.%s, prefix: \"%s\")".formatted(k, f.value());
        default -> throw new IllegalArgumentException("Unsupported TagFilter.Op: " + f.op());
      });
      sb.append(")\n");
    }

    // bucketização + agregações (exemplo genérico; ajuste conforme sua DSL)
    q.bucket().ifPresent(b ->
      sb.append("|> aggregateWindow(every: %s, fn: mean, createEmpty: false)\n".formatted(b.every()))
    );

    // order & limit (keyset por _time)
    if (q.orderByField().orElse("time").equals("time")) {
      q.seekAfter().ifPresent(after -> {
        String op = q.orderDir() == TsQuery.SortDir.ASC ? ">" : "<";
        sb.append("|> filter(fn: (r) => r._time ").append(op).append(" time(v: \"%s\"))\n".formatted(after));
      });
      sb.append("|> sort(columns: [\"_time\"], desc: %s)\n".formatted(q.orderDir()==TsQuery.SortDir.DESC));
    }
    sb.append("|> limit(n: %d)\n".formatted(q.limit()));
    return sb.toString();
  }

  @Override public void ensureMeasurement(String m, Map<String,String> options) { /* Influx cria on-the-fly; configure RP/TTL no bucket */ }
  @Override public void setRetention(String m, Duration ttl) { /* alterar retention policy do bucket/org */ }
  @Override public void dropRange(String m, Range r, Map<String,String> tags) { /* DeleteApi.delete(start, stop, predicate, bucket, org) com tenantId + tags */ }
  @Override public boolean ping() { return client.ping(); }
}
