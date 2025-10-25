package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.util.Map;

public record Resource(String type, String id, Map<String, Object> attrs) {}

