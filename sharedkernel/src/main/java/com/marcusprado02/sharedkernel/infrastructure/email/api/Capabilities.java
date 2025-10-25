package com.marcusprado02.sharedkernel.infrastructure.email.api;


public record Capabilities(
        boolean supportsInbound, boolean supportsBulk, boolean supportsTemplatesServerSide,
        boolean supportsOpenTracking, boolean supportsClickTracking
) {}

