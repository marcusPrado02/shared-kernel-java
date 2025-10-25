package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;


import java.util.*;

public final class QueryStringParser {
    public static void apply(String filterParam, String sortParam, Integer page, Integer size,
                             QueryFilterBuilder builder) {
        if (page != null || size != null) builder.page(page == null ? 0 : page, size == null ? Page.DEFAULT_SIZE : size);

        if (sortParam != null && !sortParam.isBlank()) {
            for (var s : sortParam.split(",")) {
                var trimmed = s.trim();
                if (trimmed.endsWith(",desc")) builder.sortDesc(trimmed.replace(",desc", ""));
                else if (trimmed.endsWith(",asc")) builder.sortAsc(trimmed.replace(",asc", ""));
                else builder.sortAsc(trimmed);
            }
        }

        if (filterParam == null || filterParam.isBlank()) return;

        var parts = filterParam.split(";");
        for (var p : parts) {
            if (p.isBlank()) continue;
            // formato: field:op:value[,value...]
            var segs = p.split(":", 3);
            if (segs.length < 2) throw new IllegalArgumentException("Filtro inválido: " + p);
            var field = segs[0].trim();
            var opStr = segs[1].trim().toUpperCase();
            var op = Operator.valueOf(opStr);
            if (segs.length == 2) {
                builder.where(field).op(op);
            } else {
                var raw = segs[2];
                Object[] values = (op == Operator.IN || op == Operator.NIN)
                        ? Arrays.stream(raw.split(",")).map(String::trim).toArray()
                        : new Object[]{ raw.trim() };
                builder.where(field).op(op, values);
            }
        }
    }
}