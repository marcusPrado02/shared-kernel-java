package com.marcusprado02.sharedkernel.contracts.api;
import java.util.function.Predicate;
/** Filtro da cadeia. Pode short-circuitar sem chamar o chain. */
@FunctionalInterface
public interface ApiFilter {
    FilterResult apply(ApiExchange exchange, Chain chain) throws Exception;

    interface Chain {
        FilterResult proceed(ApiExchange exchange) throws Exception;
    }

    /** Com posição e condição. */
    default FilterDef withNameOrderWhen(String name, int order, Predicate<ApiExchange> when) {
        return new FilterDef(name, this, order, when);
    }
}

