package com.marcusprado02.sharedkernel.infrastructure.search;

import java.util.Map;

public record TenantContext(String tenantId, String routingKey, Map<String, String> attributes) {}
