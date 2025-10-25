package com.marcusprado02.sharedkernel.infrastructure.payments.model;

public record WebhookResult(boolean accepted, String reason) {}