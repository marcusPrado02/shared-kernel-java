package com.marcusprado02.sharedkernel.cqrs.bus.behaviors;

import java.util.Map;

public final class Slf4jAuditSink implements AuditSink {
  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger("AUDIT");
  @Override public void record(String action, String tenant, String user, boolean ok, long ms, Map<String,Object> f, Throwable e) {
    if (ok) LOG.info("action={} tenant={} user={} ms={} fields={}", action, tenant, user, ms, f);
    else LOG.warn("action={} tenant={} user={} ms={} error={} fields={}", action, tenant, user, ms, e==null?null:e.getMessage(), f);
  }
}
