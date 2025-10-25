package com.marcusprado02.sharedkernel.domain.snapshot.strategy;

import java.util.Map;

public record Decision(boolean takeSnapshot, String reason, Map<String, Object> attributes) {
    public static Decision yes(String reason, Map<String,Object> attrs){ return new Decision(true, reason, attrs); }
    public static Decision no(String reason, Map<String,Object> attrs){ return new Decision(false, reason, attrs); }
}
