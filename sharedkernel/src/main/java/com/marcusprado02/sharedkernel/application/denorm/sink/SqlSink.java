package com.marcusprado02.sharedkernel.application.denorm.sink;


import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.marcusprado02.sharedkernel.application.denorm.DenormSink;

import java.util.Map;

public class SqlSink implements DenormSink {
    private final NamedParameterJdbcTemplate jdbc;

    public SqlSink(NamedParameterJdbcTemplate jdbc){ this.jdbc = jdbc; }

    @Override public void upsert(String table, String id, Map<String, Object> doc) {
        // Gera upsert dinâmico ON CONFLICT (id) DO UPDATE SET ...
        var cols = doc.keySet().stream().toList();
        var placeholders = ":" + String.join(",:", cols);
        var updates = String.join(",", cols.stream().filter(c -> !c.equals("id")).map(c -> c + "=EXCLUDED." + c).toList());
        var sql = "INSERT INTO " + table + " (" + String.join(",", cols) + ") VALUES (" + placeholders + ") " +
                  "ON CONFLICT (id) DO UPDATE SET " + updates;
        jdbc.update(sql, doc);
    }

    @Override public void delete(String table, String id) {
        jdbc.update("DELETE FROM " + table + " WHERE id=:id", Map.of("id", id));
    }
}
