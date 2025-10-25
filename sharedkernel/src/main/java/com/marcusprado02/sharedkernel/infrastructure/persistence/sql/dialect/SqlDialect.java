package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.dialect;

public interface SqlDialect {
  String q(String identifier); // quote id
  String placeholder(String name); // :name
  String limitOffsetClause(int limit, int offset);
  String forUpdateClause(boolean nowait);
  String upsert(String table, String[] keyCols, String[] updatableCols);
}
