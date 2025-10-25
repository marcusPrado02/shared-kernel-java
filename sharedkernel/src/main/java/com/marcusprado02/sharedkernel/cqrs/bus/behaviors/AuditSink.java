package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;


import java.util.Map;

public interface AuditSink {
  void record(String action, String tenant, String user, boolean ok, long elapsedMs, Map<String,Object> fields, Throwable error);
}
