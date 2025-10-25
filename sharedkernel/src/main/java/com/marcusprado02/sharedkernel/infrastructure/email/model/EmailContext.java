package com.marcusprado02.sharedkernel.infrastructure.email.model;

import java.util.Map;

public record EmailContext(
        String tenantId, String category, String campaignId, String localeTag,
        String preferredProvider, Map<String,Object> tags
) {}
