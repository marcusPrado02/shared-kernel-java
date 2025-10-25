package com.marcusprado02.sharedkernel.domain.policy;

import java.util.Map;
import java.util.Optional;

public final class EvalContext {
  public final String tenant;      // multi-tenant
  public final String subject;     // "user:42" | "svc:checkout"
  public final String action;      // "order.cancel" | "product.view"
  public final String resource;    // agreg/ID ou categoria
  public final Map<String,Object> attrs; // atributos ABAC (idade, status, datas...)

  public EvalContext(String tenant, String subject, String action, String resource, Map<String,Object> attrs) {
    this.tenant = tenant; this.subject = subject; this.action = action; this.resource = resource; this.attrs = Map.copyOf(attrs);
  }
  @SuppressWarnings("unchecked")
  public <T> Optional<T> get(String key, Class<T> type) {
    var v = attrs.get(key);
    return (v!=null && type.isInstance(v)) ? Optional.of((T)v) : Optional.empty();
  }
}
