package com.marcusprado02.sharedkernel.crosscutting.transformers.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public record TransformContext(
    String tenant, Locale locale, String traceId,
    Map<String, Object> attributes, Map<String, String> headers
) {
    public TransformContext withAttr(String k, Object v) {
        var copy = new HashMap<>(attributes); copy.put(k, v);
        return new TransformContext(tenant, locale, traceId, copy, headers);
    }
}
