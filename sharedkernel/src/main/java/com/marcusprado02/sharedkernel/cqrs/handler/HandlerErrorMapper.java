package com.marcusprado02.sharedkernel.cqrs.handler;

public interface HandlerErrorMapper {
    record MappedError(String code, String message, boolean retryable) {}
    MappedError map(Throwable t);
    static HandlerErrorMapper defaultMapper() {
        return t -> {
            var cls = t.getClass().getSimpleName();
            var retry = cls.contains("Timeout") || cls.contains("Transient") || cls.contains("Deadlock");
            return new MappedError(cls, t.getMessage() == null ? cls : t.getMessage(), retry);
        };
    }
}