package com.marcusprado02.sharedkernel.infrastructure.persistence.sql.example.impl;

import com.marcusprado02.sharedkernel.domain.model.base.TenantScoped;
import com.marcusprado02.sharedkernel.domain.model.base.Versioned;

public record UserAccount(
    String id,
    String tenantId,
    String email,
    String fullName,
    boolean active,
    long version
) implements Versioned, TenantScoped {
  @Override public String tenantId(){ return tenantId; }
}
