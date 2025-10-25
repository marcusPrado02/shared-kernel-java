package com.marcusprado02.sharedkernel.infrastructure.outbox;

/** Estados do outbox. */
public enum OutboxStatus { PENDING, CLAIMED, SENT, FAILED, DEAD }
