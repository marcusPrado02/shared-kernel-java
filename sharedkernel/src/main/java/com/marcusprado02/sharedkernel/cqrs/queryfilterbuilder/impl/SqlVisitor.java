package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.impl;

import java.util.*;

import com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder.*;

public final class SqlVisitor {
    public record SqlClause(String where, List<Object> params, String orderBy, int limit, int offset) {}

    public static SqlClause toSql(QueryFilter qf, FieldRegistry registry) {
        var sb = new StringBuilder();
        var params = new ArrayList<Object>();
        var first = true;

        for (var c : qf.criteria()) {
            var meta = registry.meta(c.field()).orElseThrow();
            var col = meta.internalPath(); // já mapeado para coluna/alias válido
            var op = switch (c.op()) {
                case EQ -> "="; case NE -> "!="; case GT -> ">";
                case GE -> ">="; case LT -> "<"; case LE -> "<=";
                default -> null;
            };

            if (!first) sb.append(" AND ");
            first = false;

            switch (c.op()) {
                case IN, NIN -> {
                    var placeholders = String.join(",", java.util.Collections.nCopies(c.values().size(), "?"));
                    sb.append(col).append(c.op()==Operator.NIN ? " NOT IN (" : " IN (").append(placeholders).append(")");
                    params.addAll(c.values());
                }
                case BETWEEN -> {
                    sb.append(col).append(" BETWEEN ? AND ?");
                    params.add(c.values().get(0));
                    params.add(c.values().get(1));
                }
                case LIKE, ILIKE, CONTAINS, STARTS_WITH, ENDS_WITH -> {
                    sb.append(c.op()==Operator.ILIKE ? "LOWER(" + col + ") LIKE LOWER(?)" : col + " LIKE ?");
                    params.add(pattern(c));
                }
                case IS_NULL -> sb.append(col).append(" IS NULL");
                case NOT_NULL -> sb.append(col).append(" IS NOT NULL");
                case EMPTY -> sb.append("(").append(col).append(" IS NULL OR ").append(col).append(" = '')");
                case NOT_EMPTY -> sb.append("(").append(col).append(" IS NOT NULL AND ").append(col).append(" <> '')");
                default -> {
                    sb.append(col).append(" ").append(op).append(" ?");
                    params.add(c.values().get(0));
                }
            }

            if (c.negated()) sb.insert(sb.lastIndexOf(" ") - (op == null ? 0 : 0), "NOT (").append(")");
        }

        String orderBy = "";
        if (!qf.sort().isEmpty()) {
            var parts = new ArrayList<String>();
            for (var s : qf.sort()) {
                var meta = registry.meta(s.field()).orElseThrow();
                parts.add(meta.internalPath() + (s.asc() ? " ASC" : " DESC"));
            }
            orderBy = " ORDER BY " + String.join(", ", parts);
        }

        int limit = qf.page().size;
        int offset = qf.page().number * qf.page().size;

        return new SqlClause(sb.length()==0 ? "" : "WHERE " + sb, params, orderBy, limit, offset);
    }

    private static String pattern(FilterCriterion c) {
        var v = String.valueOf(c.values().get(0));
        return switch (c.op()) {
            case STARTS_WITH -> v + "%";
            case ENDS_WITH -> "%" + v;
            case CONTAINS -> "%" + v + "%";
            case LIKE, ILIKE -> v;
            default -> v;
        };
    }
}