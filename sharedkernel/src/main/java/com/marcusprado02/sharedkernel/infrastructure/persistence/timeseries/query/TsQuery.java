package com.marcusprado02.sharedkernel.infrastructure.persistence.timeseries.query;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record TsQuery(
  String measurement,
  Range range,
  List<TagFilter> tagFilters,
  Optional<Bucket> bucket,
  List<Agg> aggregations,
  List<String> groupByTags,           // tags para GROUP BY
  Optional<String> orderByField,      // para seek pagination por campo/ts
  SortDir orderDir,                   // ASC/DESC
  Optional<Object> seekAfter,         // valor para keyset (ex: ts > last)
  int limit
) {
  public enum SortDir { ASC, DESC }
  public static Builder builder(String measurement, Range r){ return new Builder(measurement, r); }
  // Builder encurta a criação
  public static final class Builder {
    private final String m; private final Range r;
    private final List<TagFilter> filters = new ArrayList<>();
    private Optional<Bucket> bucket = Optional.empty();
    private final List<Agg> aggs = new ArrayList<>();
    private final List<String> groupTags = new ArrayList<>();
    private Optional<String> orderField = Optional.of("time");
    private SortDir dir = SortDir.ASC;
    private Optional<Object> after = Optional.empty();
    private int limit = 1000;

    public Builder tag(TagFilter f){ 
        filters.add(f); 
        return this; 
    }
    public Builder bucket(Duration every){ 
        this.bucket = Optional.of(Bucket.of(every)); 
        return this; 
    }
    public Builder agg(String field, AggFn fn, String as){ 
        aggs.add(new Agg(field, fn, as)); 
        return this; 
    }
    public Builder groupBy(String... t){ 
        groupTags.addAll(List.of(t)); 
        return this; 
    }
    public Builder order(String field, SortDir d){ 
        orderField = Optional.ofNullable(field); 
        dir = d; 
        return this; 
    }
    public Builder seekAfter(Object v){ 
        after = Optional.ofNullable(v); 
        return this; 
    }
    public Builder limit(int l){ 
        limit = l; 
        return this; 
    }
    public TsQuery build(){ 
        return new TsQuery(m, r, filters, bucket, aggs, groupTags, orderField, dir, after, limit); 
    }
    private Builder(String m, Range r){ 
        this.m = m; 
        this.r = r; 
    }
  }
}
