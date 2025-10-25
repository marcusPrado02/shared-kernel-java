package com.marcusprado02.sharedkernel.infrastructure.search;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// -------- Metadados da Query --------
public final class SearchQuery {
    private final String indexOrCollection;
    private final Optional<String> fullText;
    private final Optional<HybridQuery> hybrid;
    private final List<Criterion> filters;
    private final List<Sort> sort;
    private final PageRequest page;
    private final List<FacetRequest> facets;
    private final List<AggregationRequest> aggregations;
    private final Optional<HighlightRequest> highlight;
    private final Optional<FieldMask> fieldMask;
    private final Optional<TenantContext> tenant;
    private final boolean consistentRead; // true=quorum/majority quando suportado
    private final Duration timeout;
    private final Map<String, Object> hints; // backend-specific

    private SearchQuery(Builder b) {
        this.indexOrCollection = b.indexOrCollection;
        this.fullText = Optional.ofNullable(b.fullText);
        this.hybrid = Optional.ofNullable(b.hybrid);
        this.filters = List.copyOf(b.filters);
        this.sort = List.copyOf(b.sort);
        this.page = b.page;
        this.facets = List.copyOf(b.facets);
        this.aggregations = List.copyOf(b.aggregations);
        this.highlight = Optional.ofNullable(b.highlight);
        this.fieldMask = Optional.ofNullable(b.fieldMask);
        this.tenant = Optional.ofNullable(b.tenant);
        this.consistentRead = b.consistentRead;
        this.timeout = b.timeout != null ? b.timeout : Duration.ofSeconds(5);
        this.hints = Map.copyOf(b.hints);
    }

    public String indexOrCollection() { return indexOrCollection; }
    public Optional<String> fullText() { return fullText; }
    public Optional<HybridQuery> hybrid() { return hybrid; }
    public List<Criterion> filters() { return filters; }
    public List<Sort> sort() { return sort; }
    public PageRequest page() { return page; }
    public List<FacetRequest> facets() { return facets; }
    public List<AggregationRequest> aggregations() { return aggregations; }
    public Optional<HighlightRequest> highlight() { return highlight; }
    public Optional<FieldMask> fieldMask() { return fieldMask; }
    public Optional<TenantContext> tenant() { return tenant; }
    public boolean consistentRead() { return consistentRead; }
    public Duration timeout() { return timeout; }
    public Map<String, Object> hints() { return hints; }

    // ------- Builder / DSL -------
    public static Builder on(String indexOrCollection) { return new Builder(indexOrCollection); }
    public static final class Builder {
        private final String indexOrCollection;
        private String fullText;
        private HybridQuery hybrid;
        private final List<Criterion> filters = new ArrayList<>();
        private final List<Sort> sort = new ArrayList<>();
        private PageRequest page = PageRequest.of(0, 25);
        private final List<FacetRequest> facets = new ArrayList<>();
        private final List<AggregationRequest> aggregations = new ArrayList<>();
        private HighlightRequest highlight;
        private FieldMask fieldMask;
        private TenantContext tenant;
        private boolean consistentRead = false;
        private Duration timeout = Duration.ofSeconds(5);
        private final Map<String, Object> hints = new HashMap<>();

        private Builder(String indexOrCollection) { this.indexOrCollection = indexOrCollection; }

        public Builder text(String q) { this.fullText = q; return this; }
        public Builder hybrid(String text, float textW, VectorQuery vq, float vecW) {
            this.hybrid = new HybridQuery(text, textW, vq, vecW); return this;
        }
        public Builder add(Criterion c) { this.filters.add(c); return this; }
        public Builder sortBy(Sort s) { this.sort.add(s); return this; }
        public Builder page(PageRequest p) { this.page = p; return this; }
        public Builder facet(FacetRequest f) { this.facets.add(f); return this; }
        public Builder agg(AggregationRequest a) { this.aggregations.add(a); return this; }
        public Builder highlight(HighlightRequest h) { this.highlight = h; return this; }
        public Builder fieldMask(FieldMask m) { this.fieldMask = m; return this; }
        public Builder tenant(TenantContext t) { this.tenant = t; return this; }
        public Builder consistentRead(boolean c) { this.consistentRead = c; return this; }
        public Builder timeout(Duration t) { this.timeout = t; return this; }
        public Builder hint(String k, Object v) { this.hints.put(k, v); return this; }
        public SearchQuery build() { return new SearchQuery(this); }
    }
}
