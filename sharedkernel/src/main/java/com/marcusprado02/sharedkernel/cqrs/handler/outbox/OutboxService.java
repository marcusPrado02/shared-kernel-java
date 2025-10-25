package com.marcusprado02.sharedkernel.cqrs.handler.outbox;

public interface OutboxService {
    void append(Object message, String category, String key); // serializa/guarda p/ CDC
}
