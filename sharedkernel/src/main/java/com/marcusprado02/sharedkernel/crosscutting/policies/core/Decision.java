package com.marcusprado02.sharedkernel.crosscutting.policies.core;

import java.util.Map;

public record Decision(Effect effect,
                       String policyId,
                       String reason,
                       Map<String,Object> obligations, // ex.: { "maskFields": ["email","ssn"] }
                       String traceId) {
    public boolean isAllow() { return effect == Effect.ALLOW; }
}
