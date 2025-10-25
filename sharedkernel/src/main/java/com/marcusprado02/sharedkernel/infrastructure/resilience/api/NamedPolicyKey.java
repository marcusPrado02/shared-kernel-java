package com.marcusprado02.sharedkernel.infrastructure.resilience.api;

public record NamedPolicyKey(String namespace, String name) implements PolicyKey {}
