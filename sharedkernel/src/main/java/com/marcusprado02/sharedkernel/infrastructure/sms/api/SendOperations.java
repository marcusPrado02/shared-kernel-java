package com.marcusprado02.sharedkernel.infrastructure.sms.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.marcusprado02.sharedkernel.infrastructure.sms.model.*;

public interface SendOperations {
    SmsResponse send(SmsRequest req, Policy policy);
    List<SmsResponse> sendBulk(BulkRequest req, Policy policy);

    CompletableFuture<SmsResponse> sendAsync(SmsRequest req, Policy policy);
    CompletableFuture<List<SmsResponse>> sendBulkAsync(BulkRequest req, Policy policy);
}
