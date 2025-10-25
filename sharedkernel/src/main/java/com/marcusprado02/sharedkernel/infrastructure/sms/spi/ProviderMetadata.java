package com.marcusprado02.sharedkernel.infrastructure.sms.spi;

import java.util.Set;

public record ProviderMetadata(
        String id,
        String displayName,
        String version,
        Set<String> regions,
        Capabilities capabilities
) {}
