package com.marcusprado02.sharedkernel.application.config;


import java.util.Optional;

public class ContextMetadata {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    public static void setCorrelationId(String id) {
        CORRELATION_ID.set(id);
    }

    public static Optional<String> getCorrelationId() {
        return Optional.ofNullable(CORRELATION_ID.get());
    }

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static Optional<String> getUserId() {
        return Optional.ofNullable(USER_ID.get());
    }

    public static void clear() {
        CORRELATION_ID.remove();
        USER_ID.remove();
    }
}
