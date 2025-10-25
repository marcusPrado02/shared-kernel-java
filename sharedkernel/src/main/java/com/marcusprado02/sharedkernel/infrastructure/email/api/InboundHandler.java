package com.marcusprado02.sharedkernel.infrastructure.email.api;

import com.marcusprado02.sharedkernel.infrastructure.email.model.InboundResult;

public interface InboundHandler {
    /** Processa inbound (reply-to ou endereço de captura). */
    InboundResult handleInbound(String providerId, byte[] rawMime);
}
