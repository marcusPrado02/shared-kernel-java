package com.marcusprado02.sharedkernel.cqrs.queryfilterbuilder;


import java.time.*;
import java.util.*;
import java.util.function.Function;

public final class TypeCoercion {
    private final Map<Class<?>, Function<String, ?>> map = new HashMap<>();

    public TypeCoercion() {
        register(String.class, s -> s);
        register(Integer.class, Integer::valueOf);
        register(Long.class, Long::valueOf);
        register(Double.class, Double::valueOf);
        register(Boolean.class, s -> "1".equals(s) || "true".equalsIgnoreCase(s));
        register(LocalDate.class, LocalDate::parse);
        register(LocalDateTime.class, LocalDateTime::parse);
        register(UUID.class, UUID::fromString);
    }

    public <T> void register(Class<T> type, Function<String, T> fn) { map.put(type, fn); }

    @SuppressWarnings("unchecked")
    public <T> T coerce(String raw, Class<T> type) {
        var fn = map.get(type);
        if (fn == null) throw new IllegalArgumentException("No coercion registered for " + type);
        return (T) fn.apply(raw);
    }
}

