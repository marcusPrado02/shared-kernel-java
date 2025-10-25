package com.marcusprado02.sharedkernel.domain.policy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PolicyRegistry {
  private final Map<PolicyId, Policy> byId = new ConcurrentHashMap<>();
  public void register(Policy p, String tenant) { byId.put(new PolicyId(tenant, p.id(), p.version()), p); }
  public Policy get(String tenant, String id, String version) {
    var p = byId.get(new PolicyId(tenant,id,version));
    if (p==null) throw new IllegalStateException("policy not found: "+tenant+"/"+id+"/"+version);
    return p;
  }
}
