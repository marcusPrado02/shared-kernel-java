package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record TransformResult<O>(
    Outcome outcome, Optional<O> value, Map<String, Object> meta, List<SideOutput<?>> sideOutputs
) {
    public static <O> TransformResult<O> ok(O v) { return new TransformResult<>(Outcome.OK, Optional.ofNullable(v), Map.of(), List.of()); }
    public static <O> TransformResult<O> drop(String reason) { return new TransformResult<>(Outcome.DROP, Optional.empty(), Map.of("reason",reason), List.of()); }
    public static <O> TransformResult<O> retry(String reason, long backoffMs) {
        return new TransformResult<>(Outcome.RETRY, Optional.empty(), Map.of("reason",reason, "backoffMs", backoffMs), List.of());
    }
    public static <O> TransformResult<O> dlq(String reason) { return new TransformResult<>(Outcome.DEAD_LETTER, Optional.empty(), Map.of("reason",reason), List.of()); }
    public <T> TransformResult<O> withMeta(String k, Object v) {
        var m = new HashMap<>(meta); m.put(k, v); return new TransformResult<>(outcome, value, m, sideOutputs);
    }
}
