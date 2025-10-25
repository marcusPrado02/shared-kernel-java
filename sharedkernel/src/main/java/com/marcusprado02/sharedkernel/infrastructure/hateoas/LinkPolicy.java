package com.marcusprado02.sharedkernel.infrastructure.hateoas;

import java.net.URI;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/** Política condicional: decide se link é aplicável. */
@FunctionalInterface
public interface LinkPolicy<T> extends BiPredicate<T, LinkBuildContext> {
    static <T> LinkPolicy<T> always(){ return (t,ctx)->true; }
        static <T> LinkPolicy<T> allowAll() {
        return (t, ctx) -> true;
    }

    static <T> LinkPolicy<T> when(BiPredicate<T, LinkBuildContext> p) {
        return p::test;                             // retorna LinkPolicy<T>, não BiPredicate cru
    }

    static <T> LinkPolicy<T> when(Predicate<T> p) {
        return (t, ctx) -> p.test(t);
    }

    static <T> LinkPolicy<T> scope(String scope) {
        return (t, ctx) -> ctx != null && ctx.hasScope(scope);
    }

    default LinkPolicy<T> and(LinkPolicy<? super T> other) {
        return (t, ctx) -> this.test(t, ctx) && other.test(t, ctx);
    }

    default LinkPolicy<T> or(LinkPolicy<? super T> other) {
        return (t, ctx) -> this.test(t, ctx) || other.test(t, ctx);
    }

    default LinkPolicy<T> not() {
        return (t, ctx) -> !this.test(t, ctx);
    }
}