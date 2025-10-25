package com.marcusprado02.sharedkernel.adapters.in.grpc.interceptors;

import java.util.Map;

public interface EdgeRateLimiter {
    record Decision(boolean allowed, long remaining, long resetEpochSeconds, Map<String,Object> debug) {}
    Decision take(String key, String policyName);
}
