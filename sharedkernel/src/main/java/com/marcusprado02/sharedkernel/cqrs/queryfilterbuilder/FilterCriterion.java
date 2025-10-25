package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;

import java.util.*;

public final class FilterCriterion {
    private final String field;              // nome lógico (externo) ex: "customerId"
    private final Operator op;
    private final List<Object> values;       // coerção feita antes de construir o critério
    private final boolean negated;
    private final boolean caseInsensitive;

    private FilterCriterion(String field, Operator op, List<Object> values,
                            boolean negated, boolean caseInsensitive) {
        this.field = field;
        this.op = op;
        this.values = values == null ? List.of() : List.copyOf(values);
        this.negated = negated;
        this.caseInsensitive = caseInsensitive;
    }

    public static FilterCriterion of(String field, Operator op, List<Object> values) {
        return new FilterCriterion(field, op, values, false, false);
    }
    public FilterCriterion negatedFilterCriterion() { return new FilterCriterion(field, op, values, true, caseInsensitive); }
    public FilterCriterion ci()      { return new FilterCriterion(field, op, values, negated, true); }

    public String field() { return field; }
    public Operator op() { return op; }
    public List<Object> values() { return values; }
    public boolean negated() { return negated; }
    public boolean caseInsensitive() { return caseInsensitive; }
}
