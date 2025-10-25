package com.marcusprado02.sharedkernel.adapters.in.rest.pagination.jdbc;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.*;

/** Especifica SQL base e como extrair/suprir keyset. */
public interface JdbcQuerySpec {
    /** SQL base sem ORDER. Deve conter WHERE estático da consulta. */
    String baseSql();
    /** ORDER BY calculado a partir de sort; se reversed=true, inverte direções. */
    default String orderBy(List<Order> sort, boolean reversed){
        StringBuilder sb = new StringBuilder(" ORDER BY ");
        for (int i=0; i<sort.size(); i++){
            var o = sort.get(i);
            var dir = (reversed ^ (o.direction()==Direction.DESC)) ? "DESC" : "ASC";
            sb.append(o.field()).append(" ").append(dir);
            if (i<sort.size()-1) sb.append(", ");
        }
        return sb.toString();
    }

    /** Gera SQL final: base + (whereKeyset?) + ORDER BY. */
    default String sql(List<Order> sort, boolean reversed, String whereKeyset){
        String sql = baseSql();
        if (whereKeyset != null && !whereKeyset.isBlank()) {
            sql += " AND (" + whereKeyset + ") ";
        }
        sql += orderBy(sort, reversed);
        return sql;
    }

    String countSql();                      // "select count(*) from (...) t" ou equivalente
    int bind(PreparedStatement ps, int idx) throws SQLException;

    /** Predicado keyset, ex.: "(a, b, id) > (?, ?, ?)". */
    String keysetPredicate(CursorPayload payload, List<Order> sort, Direction dir);

    /** Preenche os ? do predicado keyset. */
    int bindKeyset(PreparedStatement ps, int idx, CursorPayload payload, List<Order> sort, Direction dir) throws SQLException;

    /** Extrai valores chave (field->value) para montar cursor a partir de uma linha. */
    Map<String,Object> extractKeyValues(Map<String,Object> row, List<Order> sort);

    default boolean canCountCheap(){ return false; }
    default boolean canApproximateTotal(){ return false; }
    default Long totalApprox(java.sql.Connection conn){ return null; }
}