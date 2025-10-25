package com.marcusprado02.sharedkernel.crosscutting.exception.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ExceptionMapperRegistry<Ctx> {
    private final Map<Class<?>, ExceptionMapper<?, Ctx>> map = new ConcurrentHashMap<>();
    public <T extends Throwable> void register(Class<T> type, ExceptionMapper<T, Ctx> mapper) {
        map.put(type, mapper);
    }
    @SuppressWarnings("unchecked")
    public MappedError map(Throwable ex, Ctx ctx) {
        Class<?> c = ex.getClass();
        while (c != null) {
            var m = (ExceptionMapper<Throwable, Ctx>) map.get(c);
            if (m != null) return m.map(ex, ctx);
            c = c.getSuperclass();
        }
        // fallback
        return MappedError.builder().status(500).code("INTERNAL_ERROR")
            .title("Internal Error").detail("Unexpected error").build();
    }
}