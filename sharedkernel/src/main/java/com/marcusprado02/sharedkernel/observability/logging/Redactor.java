package com.marcusprado02.sharedkernel.observability.logging;


import java.util.Map;
import java.util.regex.Pattern;

public final class Redactor {
    private static final String REDACTED = "[REDACTED]";
    private static final Pattern EMAIL = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+");
    private static final Pattern CPF   = Pattern.compile("\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b");
    private static final Pattern CREDIT_CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");

    public Object scrub(Object v){
        if (v instanceof String s) {
            if (EMAIL.matcher(s).find()) return REDACTED;
            if (CPF.matcher(s).find()) return REDACTED;
            if (CREDIT_CARD.matcher(s).find()) return REDACTED;
            if (s.length() > 8192) return s.substring(0, 8192) + "...";
        }
        return v;
    }

    public Map<String,Object> scrubMap(Map<String,Object> in){
        if (in == null || in.isEmpty()) return Map.of();
        java.util.LinkedHashMap<String,Object> out = new java.util.LinkedHashMap<>(in.size());
        in.forEach((k,v) -> out.put(k, scrub(v)));
        return out;
    }
}

