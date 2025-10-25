package com.marcusprado02.sharedkernel.infrastructure.email.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.email.model.*;

public interface SendOperations {
    EmailResponse send(EmailRequest req, Policy policy);
    List<EmailResponse> sendBulk(BulkRequest req, Policy policy);

    CompletableFuture<EmailResponse> sendAsync(EmailRequest req, Policy policy);
    CompletableFuture<List<EmailResponse>> sendBulkAsync(BulkRequest req, Policy policy);
}
