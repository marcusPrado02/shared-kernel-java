package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.Point;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.model.SeriesRow;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.Range;
import com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query.TsQuery;

public interface TimeSeriesRepository {
  // Ingest
  void write(Point point);
  void writeBatch(List<Point> points);                 // batching + retry + backpressure

  // Query primitivas (results como linhas); agregações e bucketização
  List<SeriesRow> query(TsQuery q);

  // Conveniências
  default List<SeriesRow> range(String measurement, Range r, String... tagEqPairs) {
    TsQuery query = TsQuery.builder(measurement, r)
      .groupBy(tagEqPairs)
      .build();
    return query(query);
  }

  // Manutenção
  void ensureMeasurement(String measurement, Map<String,String> options); // RP/TTL, índices, hypertable, compressão
  void setRetention(String measurement, Duration ttl);                    // RP/TTL/Policy
  void dropRange(String measurement, Range r, Map<String,String> tagEqFilter); // purga (LGPD)

  // Saúde
  boolean ping();
}
