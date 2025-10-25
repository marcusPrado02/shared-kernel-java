package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.dialect;

import java.util.Arrays;

public final class MySqlDialect implements SqlDialect {
  public String q(String id){ return "`" + id + "`"; }
  public String placeholder(String n){ return ":" + n; }
  public String limitOffsetClause(int limit, int offset){ return " LIMIT " + limit + " OFFSET " + offset; }
  public String forUpdateClause(boolean nowait){ return " FOR UPDATE"; }
  public String upsert(String table, String[] keys, String[] cols){
    var upd = String.join(", ", Arrays.stream(cols)
        .map(c -> q(c) + " = VALUES(" + q(c) + ")").toList());
    return " ON DUPLICATE KEY UPDATE " + upd;
  }
}
