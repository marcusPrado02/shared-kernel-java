package com.marcusprado02.sharedkernel.saga;

public interface ProcessedMessageStore {
    boolean seen(String messageId, String consumerName);
    void mark(String messageId, String consumerName);
}
