package com.marcusprado02.sharedkernel.infrastructure.email.api;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.email.model.EmailContext;
import com.marcusprado02.sharedkernel.infrastructure.email.model.EmailEvent;

public interface EmailGateway extends SendOperations, WebhookHandler, InboundHandler {
    Optional<String> resolveProvider(EmailContext ctx);
    Capabilities capabilities(String providerId);

    EmailEvent getStatus(String providerId, String messageId, EmailContext ctx);
    List<EmailEvent> getStatusBulk(String providerId, List<String> messageIds, EmailContext ctx);
}