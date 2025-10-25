package com.marcusprado02.sharedkernel.domain.model.base;


import java.util.function.Supplier;

public final class Guard {
    private Guard() {}
    public static void that(boolean condition, Supplier<String> message) {
        if (!condition) throw new IllegalStateException(message.get());
    }
    public static <T> T notNull(T value, String name) {
        if (value == null) throw new IllegalArgumentException(name + " must not be null");
        return value;
    }
}