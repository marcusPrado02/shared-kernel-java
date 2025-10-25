package com.marcusprado02.sharedkernel.infrastructure.config.featureflag.api;

import java.util.Map;

public record FlagContext(
    String tenantId,
    String userId,
    String sessionId,
    String region,
    Map<String, Object> attrs // ex.: plan, deviceType, cohort, abBucket, isStaff
) {}
