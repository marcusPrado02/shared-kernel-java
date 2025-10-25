package com.marcusprado02.sharedkernel.infrastructure.sms.model;

import java.util.List;

public record BulkRequest(List<SmsRequest> messages) {}

