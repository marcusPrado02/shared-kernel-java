package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.util.Map;
import java.util.Set;

public record Subject(String id, Set<String> roles, Map<String, Object> attrs) {}
