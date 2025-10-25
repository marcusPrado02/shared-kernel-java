package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.time.Instant;
import java.util.Map;

public record Environment(String tenant, String ip, String region, Instant now, Map<String, Object> attrs) {}
