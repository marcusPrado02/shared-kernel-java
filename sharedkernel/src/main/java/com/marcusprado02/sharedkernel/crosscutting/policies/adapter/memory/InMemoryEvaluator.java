package com.marcusprado02.sharedkernel.crosscutting.policies.adapter.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.marcusprado02.sharedkernel.crosscutting.policies.core.*;

public final class InMemoryEvaluator implements PolicyEvaluator {
    private final List<Rule> rules = new ArrayList<>();
    public record Rule(String id, Predicate<Ctx> when, Function<Ctx,Decision> then) {}
    public record Ctx(Subject s, String a, Resource r, Environment e) {}

    @Override public Decision evaluate(Subject s, String a, Resource r, Environment e) {
        var ctx = new Ctx(s,a,r,e);
        for (var rule: rules) {
            if (rule.when().test(ctx)) return rule.then().apply(ctx);
        }
        return null;
    }
}

