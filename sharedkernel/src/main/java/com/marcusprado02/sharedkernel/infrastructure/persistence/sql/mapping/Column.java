package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.mapping;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;
import java.util.function.*;

public record Column<T>(
  String name,
  BiFunction<ResultSet, String, T> reader,
  Function<T, Object> writer // adapta para JDBC param
) {
  public T read(ResultSet rs) throws SQLException { return reader.apply(rs, name); }
  public Object write(T v){ return writer.apply(v); }

  // helpers
  public static Column<String> str(String n) { 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getString(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Long>   lng(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getLong(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Boolean> bool(String n){ 
    return new Column<>(n, (rs, c) -> {
      try {
        return rs.getBoolean(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<BigDecimal> dec(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getBigDecimal(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<UUID> uuid(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return UUID.fromString(rs.getString(c));
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v.toString()); 
  }
  public static Column<Timestamp> ts(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getTimestamp(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Date> date(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getDate(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Integer> integer(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getInt(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Double> dbl(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getDouble(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
  public static Column<Float> flt(String n){ 
    return new Column<>(n, (rs,c) -> {
      try {
        return rs.getFloat(c);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }, v->v); 
  }
}
