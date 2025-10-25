package com.marcusprado02.sharedkernel.infrastructure.search.adapter.elasticsearch;


import com.marcusprado02.sharedkernel.infrastructure.search.*;

import org.elasticsearch.client.*;
import org.elasticsearch.action.search.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.*;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryBuilder;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ElasticsearchSearchAdapter<T> extends BaseSearchAdapter<T> {

    private final RestHighLevelClient client;

    public ElasticsearchSearchAdapter(RestHighLevelClient client,
                                      io.opentelemetry.api.trace.Tracer tracer,
                                      io.micrometer.core.instrument.MeterRegistry meter,
                                      io.github.resilience4j.retry.Retry retry,
                                      io.github.resilience4j.circuitbreaker.CircuitBreaker cb,
                                      CacheAdapter cache) {
        super(tracer, meter, retry, cb, cache);
        this.client = client;
    }

    @Override
    protected PageResult<T> doSearch(SearchQuery q, ProjectionMapper<Map<String, Object>, T> mapper) {
        try {
            var ssb = new SearchSourceBuilder();
            ssb.trackTotalHits(true);
            ssb.timeout(org.elasticsearch.core.TimeValue.timeValueMillis(q.timeout().toMillis()));

            // Query principal (texto, híbrida ou bool)
            QueryBuilder main;
            if (q.hybrid().isPresent()) {
                var h = q.hybrid().get();
                // Estratégia simples: should(text) + should(knn) com weights; ajustes via hints
                var should = new ArrayList<QueryBuilder>();
                if (h.text() != null && !h.text().isBlank()) {
                    should.add(QueryBuilders.simpleQueryStringQuery(h.text()));
                }
                if (h.vector() != null) {
                    // ES 8+ knn query via SearchSourceBuilder.knnSearch ou script_score (depende da versão)
                    // Aqui deixamos um placeholder para usar hints específicos de versão
                    // Ex.: ssb.knnSearch(new KnnSearchBuilder(h.vector().field(), h.vector().vector(), h.vector().k(), null));
                }
                main = QueryBuilders.boolQuery().should(should.get(0));
            } else if (q.fullText().isPresent()) {
                main = QueryBuilders.simpleQueryStringQuery(q.fullText().get());
            } else {
                main = QueryBuilders.matchAllQuery();
            }

            // Filtros
            for (var c : q.filters()) {
                main = QueryBuilders.boolQuery()
                        .must(main)
                        .filter(toEsFilter(c));
            }

            ssb.query(main);

            // Sort
            for (var s : q.sort()) {
                ssb.sort(new FieldSortBuilder(s.field())
                        .order(s.direction() == Direction.ASC ? org.elasticsearch.search.sort.SortOrder.ASC : org.elasticsearch.search.sort.SortOrder.DESC));
            }

            // Paginação / Cursor
            if (q.page().cursor() == null) {
                ssb.from(Math.max(q.page().page(), 0) * q.page().size()).size(q.page().size());
            } else {
                // search_after: esperar que o cursor encode os valores do último sort
                // placeholder: aplicar decode do cursor e setar searchAfter
            }

            // Facetas (terms agg)
            for (var f : q.facets()) {
                ssb.aggregation(org.elasticsearch.search.aggregations.AggregationBuilders
                        .terms(f.name()).field(f.field()).size(f.size()));
            }

            // Agregações adicionais
            for (var a : q.aggregations()) {
                // exemplo: avg/sum/min/max/date_histogram conforme a.type()
                // construir via AggregationBuilders
            }

            // Highlight
            q.highlight().ifPresent(h -> {
                var hb = new HighlightBuilder()
                        .requireFieldMatch(false)
                        .fragmentSize(h.fragmentSize())
                        .numOfFragments(h.numberOfFragments())
                        .preTags(h.preTag())
                        .postTags(h.postTag());
                h.fields().forEach(f -> hb.field(new HighlightBuilder.Field(f)));
                ssb.highlighter(hb);
            });

            var req = new SearchRequest(q.indexOrCollection()).source(ssb);
            var resp = client.search(req, org.elasticsearch.client.RequestOptions.DEFAULT);

            var hits = new ArrayList<T>();
            var highlights = new HashMap<String, Map<String, List<String>>>();

            Arrays.stream(resp.getHits().getHits()).forEach(h -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> source = (Map<String, Object>) h.getSourceAsMap();
                var mapped = mapper.map(source, q.fieldMask().orElse(new FieldMask(Set.of(), Set.of())));
                hits.add(mapped);

                if (h.getHighlightFields() != null && !h.getHighlightFields().isEmpty()) {
                    var perField = new HashMap<String, List<String>>();
                    h.getHighlightFields().forEach((field, fragments) -> {
                        perField.put(field, Arrays.stream(fragments.fragments()).map(org.elasticsearch.common.text.Text::string).collect(Collectors.toList()));
                    });
                    highlights.put(h.getId(), perField);
                }
            });

            // Facets
            var facetResults = new HashMap<String, FacetResult>();
            q.facets().forEach(f -> {
                var agg = resp.getAggregations().get(f.name());
                if (agg instanceof org.elasticsearch.search.aggregations.bucket.terms.Terms t) {
                    var counts = new LinkedHashMap<String, Long>();
                    t.getBuckets().forEach(b -> counts.put(b.getKeyAsString(), b.getDocCount()));
                    facetResults.put(f.name(), new FacetResult(f.name(), counts));
                }
            });

            // Aggregations genéricas (placeholder)
            var aggResults = new HashMap<String, AggregationResult>();

            return new PageResult<>(
                    hits,
                    resp.getHits().getTotalHits().value,
                    Math.max(q.page().page(), 0),
                    q.page().size(),
                    /*nextCursor*/ null,
                    Duration.ofMillis(resp.getTook().getMillis()),
                    facetResults,
                    aggResults,
                    highlights
            );

        } catch (Exception e) {
            throw new RuntimeException("Elastic search failed: " + e.getMessage(), e);
        }
    }

    private QueryBuilder toEsFilter(Criterion c) {
        if (c instanceof Criterion.FieldCriterion fc) {
            return switch (fc.op()) {
                case EQ -> QueryBuilders.termQuery(fc.path(), fc.value());
                case NE -> QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(fc.path(), fc.value()));
                case GT -> QueryBuilders.rangeQuery(fc.path()).gt(fc.value());
                case GTE -> QueryBuilders.rangeQuery(fc.path()).gte(fc.value());
                case LT -> QueryBuilders.rangeQuery(fc.path()).lt(fc.value());
                case LTE -> QueryBuilders.rangeQuery(fc.path()).lte(fc.value());
                case IN -> QueryBuilders.termsQuery(fc.path(), (Collection<?>) fc.value());
                case NIN -> QueryBuilders.boolQuery().mustNot(QueryBuilders.termsQuery(fc.path(), (Collection<?>) fc.value()));
                case BETWEEN -> {
                    var bounds = (List<?>) fc.value();
                    yield QueryBuilders.rangeQuery(fc.path()).gte(bounds.get(0)).lte(bounds.get(1));
                }
                case LIKE, MATCH -> QueryBuilders.matchQuery(fc.path(), fc.value());
                case PREFIX -> QueryBuilders.prefixQuery(fc.path(), fc.value().toString());
                case SUFFIX -> QueryBuilders.wildcardQuery(fc.path(), "*" + fc.value());
                case EXISTS -> QueryBuilders.existsQuery(fc.path());
                case GEO_DISTANCE -> {
                    // Espera { "lat":..., "lon":..., "distance":"10km" } em auxValue
                    @SuppressWarnings("unchecked")
                    var p = (Map<String, Object>) fc.auxValue();
                    yield QueryBuilders.geoDistanceQuery(fc.path())
                            .point((double) p.get("lat"), (double) p.get("lon"))
                            .distance(p.get("distance").toString());
                }
                default -> QueryBuilders.matchAllQuery();
            };
        } else if (c instanceof Criterion.BoolCriterion bc) {
            var b = QueryBuilders.boolQuery();
            bc.must().forEach(m -> b.must(toEsFilter(m)));
            bc.should().forEach(s -> b.should(toEsFilter(s)));
            bc.mustNot().forEach(n -> b.mustNot(toEsFilter(n)));
            if (bc.minimumShouldMatch() != null) b.minimumShouldMatch(bc.minimumShouldMatch());
            return b;
        } else if (c instanceof Criterion.ScriptCriterion sc) {
            return QueryBuilders.scriptQuery(new org.elasticsearch.script.Script(sc.script())); // params omitidos por brevidade
        }
        return QueryBuilders.matchAllQuery();
    }
}
