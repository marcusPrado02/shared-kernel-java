package com.marcusprado02.sharedkernel.crosscutting.formatters.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FormatterRegistry {
    private final Map<Class<?>, Formatter<?>> delegates = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, Formatter<T> f) {
        delegates.put(type, f);
    }

    @SuppressWarnings("unchecked")
    public <T> String format(T value) {
        if (value == null) return "null";
        var f = (Formatter<T>) delegates.get(value.getClass());
        if (f == null) throw new FormatException("No formatter for " + value.getClass());
        return f.format(value);
    }
}
