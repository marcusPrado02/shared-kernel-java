package com.marcusprado02.sharedkernel.events.idempotency;

public interface ConsumerInbox {
    /** Retorna true se a mensagem já foi processada. */
    boolean seen(String consumerName, String eventId);
    /** Marca como processada (após sucesso). */
    void record(String consumerName, String eventId);
}
