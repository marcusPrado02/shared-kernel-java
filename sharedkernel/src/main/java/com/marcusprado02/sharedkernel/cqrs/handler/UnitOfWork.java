package com.marcusprado02.sharedkernel.cqrs.handler;

public interface UnitOfWork {
    void begin();
    void commit();
    void rollback();
    /** Registra callback para flush final (ex.: publicar eventos, outbox). */
    void afterCommit(Runnable r);
}