package com.marcusprado02.sharedkernel.infrastructure.inbox;



/** Estados do inbox para controle de reprocessamento. */
public enum InboxStatus { RECEIVED, PROCESSING, PROCESSED, FAILED, DEAD }
