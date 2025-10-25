package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.List;

public record BulkRequest(List<EmailRequest> messages) {}
