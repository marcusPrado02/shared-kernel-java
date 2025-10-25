package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.dialect;

import java.util.Arrays;

public final class PostgresDialect implements SqlDialect {
  public String q(String id){ return "\"" + id + "\""; }
  public String placeholder(String n){ return ":" + n; }
  public String limitOffsetClause(int limit, int offset){ return " LIMIT " + limit + " OFFSET " + offset; }
  public String forUpdateClause(boolean nowait){ return nowait ? " FOR UPDATE NOWAIT" : " FOR UPDATE"; }
  public String upsert(String table, String[] keys, String[] cols){
    var excluded = String.join(", ", Arrays.stream(cols)
        .map(c -> q(c) + " = EXCLUDED." + q(c)).toList());
    var keyList = String.join(", ", Arrays.stream(keys).map(this::q).toList());
    return " ON CONFLICT (" + keyList + ") DO UPDATE SET " + excluded;
  }
}
