package com.marcusprado02.sharedkernel.infrastructure.payments.model;

import java.util.Map;

public record RiskData(
        String deviceId, String ip, String sessionId, Map<String, Object> attributes
) {}
