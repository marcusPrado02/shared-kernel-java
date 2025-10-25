package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.impl;


import com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.*;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;

import java.util.*;

public final class MongoVisitor {
    public static Bson toBson(QueryFilter qf, FieldRegistry registry) {
        var list = new ArrayList<Bson>();
        for (var c : qf.criteria()) {
            var meta = registry.meta(c.field()).orElseThrow();
            String path = meta.internalPath();
            Bson b = switch (c.op()) {
                case EQ -> Filters.eq(path, c.values().get(0));
                case NE -> Filters.ne(path, c.values().get(0));
                case GT -> Filters.gt(path, c.values().get(0));
                case GE -> Filters.gte(path, c.values().get(0));
                case LT -> Filters.lt(path, c.values().get(0));
                case LE -> Filters.lte(path, c.values().get(0));
                case IN -> Filters.in(path, c.values());
                case NIN -> Filters.nin(path, c.values());
                case BETWEEN -> Filters.and(Filters.gte(path, c.values().get(0)), Filters.lte(path, c.values().get(1)));
                case LIKE, ILIKE, CONTAINS, STARTS_WITH, ENDS_WITH ->
                        Filters.regex(path, regexForLike(c), c.op()==Operator.ILIKE ? "i" : "");
                case IS_NULL -> Filters.eq(path, null);
                case NOT_NULL -> Filters.ne(path, null);
                case EMPTY -> Filters.or(Filters.eq(path, null), Filters.eq(path, ""));
                case NOT_EMPTY -> Filters.and(Filters.ne(path, null), Filters.ne(path, ""));
                case EXISTS -> Filters.exists(path);
            };
            list.add(c.negated() ? Filters.not(b) : b);
        }
        return list.isEmpty() ? Filters.empty() : Filters.and(list);
    }

    private static String regexForLike(FilterCriterion c) {
        var v = String.valueOf(c.values().get(0));
        return switch (c.op()) {
            case STARTS_WITH -> "^" + java.util.regex.Pattern.quote(v) + ".*";
            case ENDS_WITH -> ".*" + java.util.regex.Pattern.quote(v) + "$";
            case CONTAINS -> ".*" + java.util.regex.Pattern.quote(v) + ".*";
            case LIKE, ILIKE -> java.util.regex.Pattern.quote(v);
            default -> throw new IllegalStateException();
        };
    }
}
