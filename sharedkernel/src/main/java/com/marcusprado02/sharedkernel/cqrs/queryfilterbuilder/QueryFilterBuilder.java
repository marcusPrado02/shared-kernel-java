package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;


import java.util.*;

public final class QueryFilterBuilder {
    private final FieldRegistry registry;
    private final TypeCoercion coercion;

    private final List<FilterCriterion> criteria = new ArrayList<>();
    private final List<SortOrder> sort = new ArrayList<>();
    private Page page = new Page(0, Page.DEFAULT_SIZE);
    private final Set<String> includes = new LinkedHashSet<>();
    private final Set<String> excludes = new LinkedHashSet<>();

    public QueryFilterBuilder(FieldRegistry registry, TypeCoercion coercion) {
        this.registry = registry;
        this.coercion = coercion;
    }

    // ---- API de alto nível
    public FieldStep where(String externalField) { return new FieldStep(externalField); }
    public QueryFilterBuilder and(String externalField, Operator op, Object... values){
        return where(externalField).op(op, values);
    }

    public QueryFilterBuilder sortAsc(String field){ sort.add(SortOrder.asc(field)); return this; }
    public QueryFilterBuilder sortDesc(String field){ sort.add(SortOrder.desc(field)); return this; }

    public QueryFilterBuilder page(int number, int size) { this.page = new Page(number, size); return this; }

    public QueryFilterBuilder include(String... fields){ includes.addAll(List.of(fields)); return this; }
    public QueryFilterBuilder exclude(String... fields){ excludes.addAll(List.of(fields)); return this; }

    public QueryFilter buildWithPolicies(Map<String, Object> context) {
        // Injeta tenant/softDelete se configurados (guardrails obrigatórios)
        registry.tenantField().ifPresent(tf -> {
            if (!containsField(tf) && context.get(tf) != null) {
                criteria.add(FilterCriterion.of(tf, Operator.EQ, List.of(context.get(tf))));
            }
        });
        registry.softDeleteField().ifPresent(sf -> {
            if (!containsField(sf)) {
                criteria.add(FilterCriterion.of(sf, Operator.EQ, List.of(Boolean.FALSE)));
            }
        });
        return new QueryFilter(criteria, sort, page, includes, excludes);
    }

    private boolean containsField(String external) {
        return criteria.stream().anyMatch(c -> c.field().equals(external));
    }

    // ---- Passo de campo (fluent)
    public final class FieldStep {
        private final String external;
        FieldStep(String external) { this.external = external; }

        private QueryFilterBuilder add(Operator op, Object... rawValues) {
            var meta = registry.meta(external)
                .orElseThrow(() -> new IllegalArgumentException("Campo não permitido: " + external));

            if (!meta.allowedOps().contains(op))
                throw new IllegalArgumentException("Operador " + op + " não permitido para " + external);

            var values = new ArrayList<Object>();
            var type = meta.type();

            if (op == Operator.BETWEEN) {
                if (rawValues.length != 2) throw new IllegalArgumentException("BETWEEN requer 2 valores");
            }
            for (var v : rawValues) {
                if (v == null) values.add(null);
                else if (v instanceof String s) values.add(coercion.coerce(s, type));
                else values.add(v);
            }

            criteria.add(FilterCriterion.of(external, op, values));
            return QueryFilterBuilder.this;
        }

        public QueryFilterBuilder eq(Object v) { return add(Operator.EQ, v); }
        public QueryFilterBuilder ne(Object v) { return add(Operator.NE, v); }
        public QueryFilterBuilder gt(Object v) { return add(Operator.GT, v); }
        public QueryFilterBuilder ge(Object v) { return add(Operator.GE, v); }
        public QueryFilterBuilder lt(Object v) { return add(Operator.LT, v); }
        public QueryFilterBuilder le(Object v) { return add(Operator.LE, v); }
        public QueryFilterBuilder in(Object... v){ return add(Operator.IN, v); }
        public QueryFilterBuilder nin(Object... v){ return add(Operator.NIN, v); }
        public QueryFilterBuilder between(Object a, Object b){ return add(Operator.BETWEEN, a, b); }
        public QueryFilterBuilder like(String v){ return add(Operator.LIKE, v); }
        public QueryFilterBuilder ilike(String v){ var qb = add(Operator.ILIKE, v); return qb; }
        public QueryFilterBuilder isNull(){ return add(Operator.IS_NULL); }
        public QueryFilterBuilder notNull(){ return add(Operator.NOT_NULL); }

        public QueryFilterBuilder op(Operator op, Object... v) { return add(op, v); }
    }
}