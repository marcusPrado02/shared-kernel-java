package com.marcusprado02.sharedkernel.adapters.in.rest.pagination.jdbc;


import javax.sql.DataSource;

import com.marcusprado02.sharedkernel.adapters.in.rest.pagination.*;

import java.sql.*;
import java.util.*;

public final class JdbcPaginationAdapter implements PaginationAdapter<JdbcQuerySpec, Map<String,Object>> {
    private final DataSource ds;
    private final CursorCodec codec;

    public JdbcPaginationAdapter(DataSource ds, CursorCodec codec){ this.ds = ds; this.codec = codec; }

    @Override public PageResult<Map<String, Object>> page(JdbcQuerySpec spec, PageRequestSpec pr) {
        return switch (pr.mode()){
            case OFFSET -> pageOffset(spec, pr);
            case CURSOR -> pageKeyset(spec, pr);
        };
    }

    private PageResult<Map<String,Object>> pageOffset(JdbcQuerySpec spec, PageRequestSpec pr){
        var sql = spec.sql(pr.sort(), false, null) + " LIMIT ? OFFSET ?";
        try (var conn = ds.getConnection();
             var ps = conn.prepareStatement((String) sql)) {
            int i = spec.bind(ps, 1);
            ps.setInt(i++, pr.limit()+1);
            ps.setInt(i, pr.offset());
            var rs = ps.executeQuery();
            var rows = read(rs);
            boolean hasMore = rows.size() > pr.limit();
            var items = hasMore ? rows.subList(0, pr.limit()) : rows;
            Long total = spec.canCountCheap() ? count(conn, spec) : null;
            String next = null;
            if (hasMore) {
                var last = items.get(items.size()-1);
                var keyVals = spec.extractKeyValues(last, pr.sort());
                next = codec.encode(codec.makeFromRow(keyVals, Direction.ASC));
            }
            return new PageResult<>(items, hasMore, total, null, next, null);
        } catch (SQLException e){ throw new RuntimeException(e); }
    }

    private PageResult<Map<String,Object>> pageKeyset(JdbcQuerySpec spec, PageRequestSpec pr){
        var after = codec.decode(pr.after());
        var before = codec.decode(pr.before());
        var dir = (before != null) ? Direction.DESC : Direction.ASC;
        var payload = (before != null) ? before : after;

        var whereKeyset = spec.keysetPredicate(payload, pr.sort(), dir);
        var sql = spec.sql(pr.sort(), before!=null, whereKeyset) + " LIMIT ?";
        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            int i = spec.bind(ps, 1);
            i = spec.bindKeyset(ps, i, payload, pr.sort(), dir);
            ps.setInt(i, pr.limit()+1);
            var rs = ps.executeQuery();
            var raw = read(rs);
            var items = (before != null) ? reverseAndTrim(raw, pr.limit()) : trim(raw, pr.limit());
            boolean hasMore = raw.size() > pr.limit();
            String next = null; String prev = null;
            if (!items.isEmpty()) {
                var first = items.get(0);
                var last  = items.get(items.size()-1);
                next = hasMore ? codec.encode(codec.makeFromRow(spec.extractKeyValues(last, pr.sort()), Direction.ASC)) : null;
                prev = codec.encode(codec.makeFromRow(spec.extractKeyValues(first, pr.sort()), Direction.DESC));
            }
            Long approx = spec.canApproximateTotal() ? spec.totalApprox(conn) : null;
            return new PageResult<>(items, hasMore, null, approx, next, prev);
        } catch (SQLException e){ throw new RuntimeException(e); }
    }

    private static List<Map<String,Object>> read(ResultSet rs) throws SQLException {
        List<Map<String,Object>> out = new ArrayList<>();
        var md = rs.getMetaData();
        while (rs.next()){
            Map<String,Object> row = new LinkedHashMap<>();
            for (int c=1; c<=md.getColumnCount(); c++){
                row.put(md.getColumnLabel(c), rs.getObject(c));
            }
            out.add(row);
        }
        return out;
    }

    private static List<Map<String,Object>> trim(List<Map<String,Object>> rows, int limit){
        return rows.size() > limit ? rows.subList(0, limit) : rows;
    }
    private static List<Map<String,Object>> reverseAndTrim(List<Map<String,Object>> rows, int limit){
        Collections.reverse(rows);
        return trim(rows, limit);
    }

    private long count(Connection conn, JdbcQuerySpec spec) throws SQLException {
        try (var ps = conn.prepareStatement(spec.countSql())) {
            spec.bind(ps, 1);
            try (var rs = ps.executeQuery()){ rs.next(); return rs.getLong(1); }
        }
    }
}

