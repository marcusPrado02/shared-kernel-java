package com.marcusprado02.sharedkernel.crosscutting.sanitize.adapters;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.marcusprado02.sharedkernel.crosscutting.sanitize.core.*;

public final class MapSanitizer implements Sanitizer<Map<String,Object>> {
    private final Map<String, Sanitizer<String>> fieldSanitizers;
    private final Function<String, Sanitizer<String>> defaultSanitizer;

    public MapSanitizer(Map<String, Sanitizer<String>> fieldSanitizers,
                        Function<String, Sanitizer<String>> defaultSanitizer){
        this.fieldSanitizers = Map.copyOf(fieldSanitizers);
        this.defaultSanitizer = defaultSanitizer;
    }

    @Override public Map<String, Object> sanitize(Map<String, Object> input, SanitizationContext ctx) {
        if (input == null) return Map.of();
        Map<String,Object> out = new HashMap<>(input.size());
        for (var e : input.entrySet()){
            Object v = e.getValue();
            if (v instanceof String s) {
                var san = fieldSanitizers.getOrDefault(e.getKey(), defaultSanitizer.apply(e.getKey()));
                out.put(e.getKey(), san!=null ? san.sanitize(s, ctx) : s);
            } else {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }
}