package com.marcusprado02.sharedkernel.infrastructure.email.core;


public interface OutboxPublisher {
    void publish(String type, Object payload); // EMAIL.SENT, EMAIL.DELIVERED, EMAIL.BOUNCED ...
}
