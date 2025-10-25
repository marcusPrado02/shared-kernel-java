package com.marcusprado02.sharedkernel.crosscutting.sanitize.core;

import java.util.ArrayList;
import java.util.List;

public final class PipelineSanitizer<T> implements Sanitizer<T> {
    private final List<Rule<T>> rules;
    private final List<RejectIf<T>> validations;

    public PipelineSanitizer(List<Rule<T>> rules, List<RejectIf<T>> validations){
        this.rules = List.copyOf(rules);
        this.validations = List.copyOf(validations);
    }

    @Override public T sanitize(T input, SanitizationContext ctx) {
        T v = input;
        for (Rule<T> r : rules) v = r.apply(v, ctx);
        for (RejectIf<T> vld : validations) vld.check(v, ctx);
        return v;
    }

    public static final class Builder<T> {
        private final List<Rule<T>> rules = new ArrayList<>();
        private final List<RejectIf<T>> validations = new ArrayList<>();
        public Builder<T> rule(Rule<T> r){ rules.add(r); return this; }
        public Builder<T> validate(RejectIf<T> v){ validations.add(v); return this; }
        public PipelineSanitizer<T> build(){ return new PipelineSanitizer<>(rules, validations); }
    }
}
