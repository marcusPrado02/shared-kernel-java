package com.marcusprado02.sharedkernel.cqrs.queryhandler.support;

public interface ErrorMapper {
    record Mapped(String code, String message, boolean clientFault) {}
    Mapped map(Throwable t);
    static ErrorMapper defaultMapper(){
        return t -> {
            var n = t.getClass().getSimpleName().toLowerCase();
            var client = n.contains("illegalargument") || n.contains("validation") || n.contains("security");
            return new Mapped(t.getClass().getSimpleName(), t.getMessage(), client);
        };
    }
}
